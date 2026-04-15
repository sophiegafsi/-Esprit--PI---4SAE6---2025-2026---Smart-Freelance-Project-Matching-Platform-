// @ts-nocheck
import { AfterViewInit, Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { CreateBadgeComponent } from './badge/create-badge/create-badge.component';
import { createEvaluationApi } from './evaluation/evaluation.api';
import { createBadgeApi } from './badge/badge.api';
import { confirmDialog, promptDialog } from './shared/dialog.util';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [NgIf, CreateBadgeComponent],
  template: `
    <app-create-badge *ngIf="showCreateBadge"></app-create-badge>

    <div class="app-shell" [hidden]="showCreateBadge">
      <header class="topbar">
        <a class="brand" href="#dashboard" aria-label="Evaluation and rewards">
          <img src="public/reward-mark.svg" alt="" />
          <span>
            <strong>Freelink Rewards</strong>
            <small>Evaluation and rewards</small>
          </span>
        </a>

        <nav class="main-nav" aria-label="Main navigation">
          <button type="button" data-page="dashboard">Dashboard</button>
          <button type="button" data-page="evaluations">Evaluations</button>
          <button type="button" data-page="badges">Badges</button>
          <button type="button" data-page="recompenses">Rewards</button>
          <button type="button" data-page="freelancers">Freelancers</button>
          <button type="button" data-page="history">History</button>
          <button type="button" data-page="space">My space</button>
        </nav>

        <div class="topbar-actions">
          <button class="primary quick-create-button" type="button" data-page-link="evaluations">+ Create evaluation</button>
          <button class="icon-button settings-button" type="button" data-page="settings" title="Settings" aria-label="Settings">⚙</button>
        </div>
      </header>

      <main id="app" class="page" tabindex="-1"></main>
    </div>

    <div id="toast-region" class="toast-region" aria-live="polite" aria-atomic="true" [hidden]="showCreateBadge"></div>
  `
})
export class AppComponent implements AfterViewInit {
  readonly showCreateBadge = this.isCreateBadgePath();

  ngAfterViewInit(): void {
    if (!this.showCreateBadge) {
      startFreelinkRewardsFront();
    }
  }

  private isCreateBadgePath(): boolean {
    if (typeof window === 'undefined') {
      return false;
    }

    return window.location.pathname.replace(/\/+$/, '') === '/create-badge';
  }
}

function startFreelinkRewardsFront(): void {const DEFAULT_CONFIG = {
  evaluationBaseUrl: 'http://localhost:8088',
  rewardBaseUrl: 'http://localhost:8088'
};

const STORAGE_KEYS = {
  config: 'freelink-front-config',
  userEmail: 'freelink-front-user-email'
};

const state = {
  page: 'dashboard',
  config: loadConfig(),
  userEmail: localStorage.getItem(STORAGE_KEYS.userEmail) || '',
  editingBadge: null,
  editingReward: null,
  historyEmail: '',
  lastSyncResponse: null
};

const app = document.querySelector('#app');
const navButtons = Array.from(document.querySelectorAll('[data-page]'));
const toastRegion = document.querySelector('#toast-region');

const pages = {
  dashboard: renderDashboard,
  evaluations: renderEvaluations,
  badges: renderBadges,
  recompenses: renderRecompenses,
  freelancers: renderFreelancers,
  history: renderHistory,
  space: renderSpace,
  settings: renderSettings
};

init();

function init() {
  navButtons.forEach((button) => {
    button.addEventListener('click', () => navigate(button.dataset.page));
  });

  window.addEventListener('hashchange', () => {
    const page = getPageFromHash();
    if (page !== state.page) {
      navigate(page, false);
    }
  });

  navigate(getPageFromHash(), false);
}

function getPageFromHash() {
  const page = window.location.hash.replace('#', '');
  return pages[page] ? page : 'dashboard';
}

async function navigate(page, updateHash = true) {
  state.page = pages[page] ? page : 'dashboard';
  state.editingBadge = state.page === 'badges' ? state.editingBadge : null;
  state.editingReward = state.page === 'recompenses' ? state.editingReward : null;

  if (updateHash && window.location.hash !== `#${state.page}`) {
    window.location.hash = state.page;
    return;
  }

  navButtons.forEach((button) => {
    button.hidden = false;
    button.classList.toggle('active', button.dataset.page === state.page);
  });

  app.innerHTML = loadingMarkup();
  app.focus({ preventScroll: true });

  try {
    await pages[state.page]();
    navButtons.forEach((button) => {
      button.hidden = false;
      button.classList.toggle('active', button.dataset.page === state.page);
    });
  } catch (error) {
    renderFatalError(error);
  }
}

function loadConfig() {
  try {
    return { ...DEFAULT_CONFIG, ...(JSON.parse(localStorage.getItem(STORAGE_KEYS.config)) || {}) };
  } catch {
    return { ...DEFAULT_CONFIG };
  }
}

function saveConfig(nextConfig) {
  state.config = { ...state.config, ...nextConfig };
  localStorage.setItem(STORAGE_KEYS.config, JSON.stringify(state.config));
}

async function request(service, path, options = {}) {
  const baseUrl = service === 'evaluation'
    ? state.config.evaluationBaseUrl
    : state.config.rewardBaseUrl;

  const headers = new Headers(options.headers || {});
  headers.set('Accept', options.responseType === 'blob' ? '*/*' : 'application/json');

  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${baseUrl}${path}`, {
    method: options.method || 'GET',
    headers,
    body: options.body === undefined
      ? undefined
      : options.body instanceof FormData
        ? options.body
        : JSON.stringify(options.body)
  });

  if (!response.ok) {
    const text = await response.text().catch(() => '');
    throw new Error(`${response.status} ${response.statusText}${text ? ` - ${text}` : ''}`);
  }

  if (options.responseType === 'blob') {
    return response.blob();
  }

  if (options.responseType === 'text') {
    return response.text();
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

const evaluationApi = createEvaluationApi(request);
const recompenseApi = createBadgeApi(request);

async function settleObject(entries) {
  const pairs = await Promise.all(Object.entries(entries).map(async ([key, promise]) => {
    try {
      return [key, { ok: true, data: await promise }];
    } catch (error) {
      return [key, { ok: false, error }];
    }
  }));

  return Object.fromEntries(pairs);
}

function pageHeader(eyebrow, title, actions = '') {
  return `
    <section class="page-header">
      <div>
        <div class="eyebrow">${escapeHtml(eyebrow)}</div>
        <h1>${escapeHtml(title)}</h1>
      </div>
      <div class="toolbar">${actions}</div>
    </section>
  `;
}

async function renderDashboard() {
  const settled = await settleObject({
    dashboard: recompenseApi.dashboard(),
    profiles: recompenseApi.profiles.list(),
    badges: recompenseApi.badges.list(),
    rewards: recompenseApi.rewards.list(),
    evaluations: evaluationApi.evaluations.list()
  });

  const dashboard = settled.dashboard.ok ? settled.dashboard.data : null;
  const profiles = settled.profiles.ok ? settled.profiles.data : [];
  const badges = settled.badges.ok ? settled.badges.data : [];
  const rewards = settled.rewards.ok ? settled.rewards.data : [];
  const evaluations = settled.evaluations.ok ? settled.evaluations.data : [];
  const rewardError = firstError(settled.dashboard, settled.profiles, settled.badges, settled.rewards);

  app.innerHTML = `
    ${pageHeader('Operations', 'Evaluation & Rewards', `
      <button class="ghost" type="button" data-action="refresh" title="Refresh">Refresh</button>
    `)}
    ${rewardError ? alertMarkup(`Reward API: ${rewardError.message}`, 'error') : ''}
    <section class="grid four">
      ${metricCard('Assigned badges', dashboard?.totalBadgesAssigned ?? 0, 'AWARDED history', 'teal')}
      ${metricCard('Active badges', dashboard?.activeBadges ?? 0, 'Active assignments', 'gold')}
      ${metricCard('Freelancers without reward', dashboard?.freelancersWithoutRewardCount ?? 0, 'Needs review', 'rose')}
      ${metricCard('Evaluations', evaluations.length, 'Evaluation microservice', 'ink')}
    </section>

    <section class="split" style="margin-top:18px">
      <div class="panel">
        <h2>Top freelancers</h2>
        ${topFreelancersMarkup(dashboard?.topFreelancers || profiles.slice(0, 10))}
      </div>
      <div class="panel">
        <h2>Monthly progress</h2>
        ${monthlyProgressMarkup(dashboard?.monthlyProgress || [])}
      </div>
    </section>

    <section class="grid three" style="margin-top:18px">
      ${metricCard('Most frequent badge', dashboard?.mostFrequentBadge || 'None', 'Reward catalog', 'teal')}
      ${metricCard('Catalog badges', badges.length, `${badges.filter((badge) => truthy(badge.isActive)).length} active`, 'gold')}
      ${metricCard('Rewards', rewards.length, `${rewards.filter((reward) => truthy(reward.isActive)).length} active`, 'rose')}
    </section>

    <section class="grid two" style="margin-top:18px">
      <div class="panel">
        <h2>Without reward</h2>
        ${simpleListMarkup(dashboard?.freelancersWithoutReward || [], 'All freelancers already have a reward.')}
      </div>
      <div class="panel">
        <h2>Latest evaluations</h2>
        ${evaluationsTableMarkup(evaluations.slice(-6).reverse(), true)}
      </div>
    </section>
  `;

  wireCommonActions();
  qs('[data-action="refresh"]')?.addEventListener('click', () => navigate('dashboard'));
}

async function renderEvaluations() {
  const settled = await settleObject({
    evaluations: evaluationApi.evaluations.list(),
    sentiment: evaluationApi.sentiment.stats()
  });

  const evaluations = settled.evaluations.ok ? settled.evaluations.data : [];
  const sentiment = settled.sentiment.ok ? settled.sentiment.data : null;

  app.innerHTML = `
    ${pageHeader('Client / Freelancer', 'Create evaluation', `
      <button class="ghost" type="button" data-action="refresh" title="Refresh">Refresh</button>
    `)}
    ${!settled.evaluations.ok ? alertMarkup(`Evaluation API: ${settled.evaluations.error.message}`, 'error') : ''}

    <section class="split">
      <form id="evaluation-form" class="form-card">
        <h2>Freelancer evaluation</h2>
        <div class="form-grid">
          ${inputField('projectName', 'Project', 'Marketplace redesign', true)}
          ${selectField('typeEvaluation', 'Type', ['SOFT_SKILLS', 'TECHNIQUE', 'AUTRE'], 'TECHNIQUE')}
          ${inputField('evaluatorName', 'Evaluator', 'Client Demo', true)}
          ${inputField('userEmail', 'Client email', state.userEmail || 'client@freelink.local', true, 'email')}
          ${inputField('evaluatedUserName', 'Freelancer', 'Amina Trabelsi', true)}
          ${inputField('evaluatedUserEmail', 'Freelancer email', 'amina.freelancer@freelink.local', true, 'email')}
          ${textareaField('comment', 'Comment', 'Clear delivery, smooth communication, and on-time completion.')}
          ${toggleField('anonymous', 'Anonymous evaluation', false)}
          <div class="criteria-grid">
            ${criterionField('quality', 'Quality', 5)}
            ${criterionField('communication', 'Communication', 4)}
            ${criterionField('deadline', 'Deadline', 5)}
            ${criterionField('expertise', 'Expertise', 4)}
          </div>
          <div class="result-box visible">
            Calculated score: <strong id="score-preview">4.50</strong> / 5
          </div>
          <div class="form-actions">
            <button class="primary" type="submit">+ Save</button>
            <button class="ghost" type="reset">Reset</button>
          </div>
        </div>
      </form>

      <form id="sync-form" class="form-card" hidden>
        <h2>Automatic assignment</h2>
        <div class="form-grid">
          ${inputField('freelancerEmail', 'Freelancer email', 'amina.freelancer@freelink.local', true, 'email')}
          ${inputField('freelancerName', 'Freelancer name', 'Amina Trabelsi', true)}
          ${numberField('currentScore', 'Current score', 5, 0, 5, 1)}
          ${numberField('averageScore', 'Average score', 4.6, 0, 5, 0.1)}
          ${numberField('totalEvaluations', 'Evaluations', 9, 0, 500, 1)}
          ${numberField('positiveEvaluations', 'Positive evaluations', 8, 0, 500, 1)}
          ${numberField('completedProjects', 'Completed projects', 10, 0, 500, 1)}
          ${numberField('totalPoints', 'Points', 560, 0, 100000, 10)}
          ${inputField('projectName', 'Project', 'Marketplace redesign', false)}
          ${inputField('evaluatedAt', 'Date', localDateTimeValue(), false, 'datetime-local')}
          <div id="sync-result" class="result-box ${state.lastSyncResponse ? 'visible' : ''}">
            ${state.lastSyncResponse ? rewardResponseMarkup(state.lastSyncResponse) : ''}
          </div>
          <div class="form-actions">
            <button class="secondary" type="submit">Run test</button>
          </div>
        </div>
      </form>
    </section>

    <section class="grid three" style="margin-top:18px">
      ${metricCard('Evaluations', evaluations.length, 'Total records', 'teal')}
      ${metricCard('Average score', averageEvaluationScore(evaluations), 'All evaluations', 'gold')}
      ${metricCard('Sentiments', sentiment ? Object.values(sentiment).reduce((sum, value) => sum + Number(value || 0), 0) : 'N/A', 'Analyzed reviews', 'rose')}
    </section>

    <section class="panel" style="margin-top:18px">
      <h2>Recent evaluations</h2>
      ${evaluationsTableMarkup([...evaluations].reverse())}
    </section>
  `;

  wireCommonActions();
  wireEvaluationForm();
  wireSyncForm();
  wireEvaluationDelete();
  qs('[data-action="refresh"]')?.addEventListener('click', () => navigate('evaluations'));
}

async function renderBadges() {
  let badges = [];
  let error = null;
  try {
    badges = await recompenseApi.badges.list();
  } catch (caught) {
    error = caught;
  }

  const editing = state.editingBadge;

  app.innerHTML = `
    ${pageHeader('Rewards', 'Badges', `
      <button class="ghost" type="button" data-action="refresh" title="Refresh">Refresh</button>
    `)}
    ${error ? alertMarkup(`API badges: ${error.message}`, 'error') : ''}

    <section class="split">
      <form id="badge-form" class="form-card">
        <h2>${editing ? 'Edit badge' : 'Create badge'}</h2>
        <div class="form-grid">
          ${inputField('name', 'Name', editing?.name || 'Expert', true)}
          ${inputField('icon', 'Icon', editing?.icon || '*', false)}
          ${selectField('conditionType', 'Condition', ['AVERAGE_SCORE', 'POINTS'], editing?.conditionType || 'AVERAGE_SCORE')}
          ${numberField('conditionValue', 'Threshold', editing?.conditionValue ?? 4.5, 0, 100000, 0.1)}
          ${numberField('pointsReward', 'Bonus points', editing?.pointsReward ?? 0, 0, 100000, 1)}
          ${textareaField('description', 'Description', editing?.description || 'Badge automatically assigned based on average score.')}
          ${toggleField('certificateEligible', 'Certificate', editing ? truthy(editing.certificateEligible) : false)}
          ${toggleField('isActive', 'Active', editing ? truthy(editing.isActive) : true)}
          <div class="form-actions">
            <button class="primary" type="submit">${editing ? 'Update' : '+ Create'}</button>
            ${editing ? '<button class="ghost" type="button" data-action="cancel-edit">Cancel</button>' : ''}
          </div>
        </div>
      </form>

      <div class="panel">
        <h2>Automatic assignment</h2>
        <div class="grid two">
          ${metricCard('Average score', 'AVERAGE_SCORE', 'Assign badge after each evaluation average', 'teal')}
          ${metricCard('Points', 'POINTS', 'Assign badge from cumulative points', 'gold')}
        </div>
      </div>
    </section>

    <section style="margin-top:18px">
      <div class="list-grid">
        ${badges.length ? badges.map(badgeCardMarkup).join('') : emptyMarkup('No badges found.')}
      </div>
    </section>
  `;

  wireCommonActions();
  wireBadgeForm();
  wireBadgeCards(badges);
  qs('[data-action="refresh"]')?.addEventListener('click', () => navigate('badges'));
  qs('[data-action="cancel-edit"]')?.addEventListener('click', () => {
    state.editingBadge = null;
    navigate('badges');
  });
}

async function renderRecompenses() {
  let rewards = [];
  let error = null;
  try {
    rewards = await recompenseApi.rewards.list();
  } catch (caught) {
    error = caught;
  }

  const editing = state.editingReward;

  app.innerHTML = `
    ${pageHeader('Rewards', 'Rewards', `
      <button class="ghost" type="button" data-action="refresh" title="Refresh">Refresh</button>
    `)}
    ${error ? alertMarkup(`Rewards API: ${error.message}`, 'error') : ''}

    <section class="split">
      <form id="reward-form" class="form-card">
        <h2>${editing ? 'Edit reward' : 'Create reward'}</h2>
        <div class="form-grid">
          ${inputField('title', 'Title', editing?.title || 'Premium voucher', true)}
          ${numberField('pointsRequired', 'Required points', editing?.pointsRequired ?? 500, 0, 100000, 10)}
          ${numberField('stock', 'Stock', editing?.stock ?? -1, -1, 100000, 1)}
          ${inputField('imageUrl', 'Image URL', editing?.imageUrl || '', false, 'url')}
          ${textareaField('description', 'Description', editing?.description || 'Reward reserved for top freelancers.')}
          ${toggleField('isActive', 'Active', editing ? truthy(editing.isActive) : true)}
          <div class="form-actions">
            <button class="primary" type="submit">${editing ? 'Update' : '+ Create'}</button>
            ${editing ? '<button class="ghost" type="button" data-action="cancel-reward-edit">Cancel</button>' : ''}
          </div>
        </div>
      </form>

      <div class="panel">
        <h2>Active catalog</h2>
        <div class="grid two">
          ${metricCard('Rewards', rewards.length, 'All entries', 'teal')}
          ${metricCard('Active', rewards.filter((reward) => truthy(reward.isActive)).length, 'Available', 'gold')}
        </div>
      </div>
    </section>

    <section style="margin-top:18px">
      <div class="list-grid">
        ${rewards.length ? rewards.map(rewardCardMarkup).join('') : emptyMarkup('No rewards found.')}
      </div>
    </section>
  `;

  wireCommonActions();
  wireRewardForm();
  wireRewardCards(rewards);
  qs('[data-action="refresh"]')?.addEventListener('click', () => navigate('recompenses'));
  qs('[data-action="cancel-reward-edit"]')?.addEventListener('click', () => {
    state.editingReward = null;
    navigate('recompenses');
  });
}

async function renderFreelancers() {
  let profiles = [];
  let insights = [];
  const settled = await settleObject({
    profiles: recompenseApi.profiles.list(),
    insights: recompenseApi.insights.list()
  });
  profiles = settled.profiles.ok ? settled.profiles.data : [];
  insights = settled.insights.ok ? settled.insights.data : [];
  const error = firstError(settled.profiles, settled.insights);
  const insightsByEmail = Object.fromEntries(insights.map((insight) => [insight.userEmail, insight]));

  app.innerHTML = `
    ${pageHeader('Rewards', 'Freelancers', `
      <button class="ghost" type="button" data-action="refresh" title="Refresh">Refresh</button>
      <button class="secondary" type="button" data-action="recalculate">Recalculate levels</button>
    `)}
    ${error ? alertMarkup(`Profiles API: ${error.message}`, 'error') : ''}

    <section class="toolbar" style="justify-content:flex-end;margin-bottom:18px">
      <button class="primary" type="button" data-action="assign-pending-rewards">Assign eligible rewards</button>
    </section>

    <section class="grid four">
      ${metricCard('Profiles', profiles.length, 'Synced freelancers', 'teal')}
      ${metricCard('Total points', sumBy(profiles, 'totalPoints'), 'All profiles', 'gold')}
      ${metricCard('Eligible', sumBy(insights, 'eligibleRecompensesCount'), 'Rewards to unlock', 'rose')}
      ${metricCard('Watch list', insights.filter((insight) => insight.performanceStatus === 'NEEDS_ATTENTION').length, 'Score to monitor', 'ink')}
    </section>

    <section class="panel" style="margin-top:18px">
      <div class="toolbar" style="justify-content:space-between;margin-bottom:14px">
        <h2 style="margin:0">Reward profiles</h2>
        <input id="profile-filter" type="search" placeholder="Filter by email, name, or badge" style="max-width:360px" />
      </div>
      ${profilesTableMarkup(profiles, insightsByEmail)}
    </section>
  `;

  wireCommonActions();
  qs('[data-action="refresh"]')?.addEventListener('click', () => navigate('freelancers'));
  qs('[data-action="recalculate"]')?.addEventListener('click', async () => {
    await guardedAction(async () => {
      const response = await recompenseApi.advanced.recalculateLevels();
      toast(response?.message || 'Levels recalculated.', 'success');
      await navigate('freelancers');
    });
  });
  qs('[data-action="assign-pending-rewards"]')?.addEventListener('click', async () => {
    await guardedAction(async () => {
      const response = await recompenseApi.advanced.assignEligibleRewards();
      toast(`${response?.assignedRewards ?? 0} reward(s) assigned.`, 'success');
      await navigate('freelancers');
    });
  });
  qs('#profile-filter')?.addEventListener('input', (event) => filterRows(event.target.value, '[data-profile-row]'));
  qsa('[data-profile-email]').forEach((button) => {
    button.addEventListener('click', () => {
      state.userEmail = button.dataset.profileEmail;
      localStorage.setItem(STORAGE_KEYS.userEmail, state.userEmail);
      navigate('space');
    });
  });
}

async function renderHistory() {
  const query = state.historyEmail.trim()
    ? `?email=${encodeURIComponent(state.historyEmail.trim())}`
    : '';

  let history = [];
  let error = null;
  try {
    history = await recompenseApi.history.list(state.historyEmail.trim());
  } catch (caught) {
    error = caught;
  }

  app.innerHTML = `
    ${pageHeader('Rewards', 'Reward history', `
      <button class="ghost" type="button" data-action="refresh" title="Refresh">Refresh</button>
    `)}
    ${error ? alertMarkup(`History API: ${error.message}`, 'error') : ''}

    <section class="panel">
      <form id="history-filter-form" class="toolbar" style="justify-content:space-between;margin-bottom:14px">
        <h2 style="margin:0">Events</h2>
        <div class="toolbar">
          <input name="email" type="email" placeholder="freelancer@email.com" value="${attr(state.historyEmail)}" style="min-width:260px" />
          <button class="primary" type="submit">Filter</button>
          <button class="ghost" type="button" data-action="clear-history-filter">Clear</button>
        </div>
      </form>
      ${historyTableMarkup(history)}
    </section>
  `;

  wireCommonActions();
  qs('[data-action="refresh"]')?.addEventListener('click', () => navigate('history'));
  qs('#history-filter-form')?.addEventListener('submit', (event) => {
    event.preventDefault();
    state.historyEmail = new FormData(event.currentTarget).get('email') || '';
    navigate('history');
  });
  qs('[data-action="clear-history-filter"]')?.addEventListener('click', () => {
    state.historyEmail = '';
    navigate('history');
  });
  wireHistoryActions();
}

async function renderSpace() {
  const email = state.userEmail.trim();

  if (!email) {
    app.innerHTML = `
      ${pageHeader('Client / Freelancer', 'My space', `
        <button class="primary" type="button" data-page-link="evaluations">Create evaluation</button>
      `)}
      <section class="form-card">
        <form id="space-email-form" class="form-grid">
          ${inputField('email', 'User email', 'amina.freelancer@freelink.local', true, 'email')}
          <div class="form-actions">
            <button class="primary" type="submit">Open</button>
          </div>
        </form>
      </section>
    `;
    wireCommonActions();
    wireSpaceEmailForm();
    return;
  }

  const settled = await settleObject({
    evaluations: evaluationApi.evaluations.list(),
    profile: recompenseApi.profiles.getByEmail(email),
    insight: recompenseApi.insights.byEmail(email),
    points: recompenseApi.points.byUser(email),
    progress: recompenseApi.points.progress(email),
    badges: recompenseApi.badges.activeForUser(email),
    history: recompenseApi.history.list(email),
    notifications: recompenseApi.notifications.byUser(email)
  });

  const evaluations = settled.evaluations.ok ? settled.evaluations.data : [];
  const relatedEvaluations = filterEvaluationsForUser(evaluations, email);
  const profile = settled.profile.ok ? settled.profile.data : null;
  const insight = settled.insight.ok ? settled.insight.data : null;
  const points = settled.points.ok ? settled.points.data : profile?.totalPoints ?? 0;
  const progress = settled.progress.ok ? settled.progress.data : 0;
  const badges = settled.badges.ok ? settled.badges.data : [];
  const history = settled.history.ok ? settled.history.data : [];
  const notifications = settled.notifications.ok ? settled.notifications.data : [];
  const error = firstError(settled.evaluations, settled.history, settled.notifications);

  app.innerHTML = `
    ${pageHeader('Client / Freelancer', profile?.userName || email, `
      <button class="primary" type="button" data-page-link="evaluations">Create evaluation</button>
      <button class="ghost" type="button" data-action="change-space-email">Change</button>
      <button class="ghost" type="button" data-action="refresh" title="Refresh">Refresh</button>
    `)}
    ${error ? alertMarkup(`Workspace API: ${error.message}`, 'error') : ''}

    <section class="grid four">
      ${metricCard('Average score', profile?.averageScore ?? 0, `${profile?.totalEvaluations ?? 0} evaluations`, 'teal')}
      ${metricCard('Points', points, `${progress}% to next milestone`, 'gold')}
      ${metricCard('Level', profile?.currentLevel || 'N/A', 'Reward profile', 'rose')}
      ${metricCard('Badges', badges.length, 'Active badges', 'ink')}
    </section>

    <section class="panel" style="margin-top:18px">
      <h2>Progression points</h2>
      <div class="progress-track"><div class="progress-bar" style="--value:${clamp(progress, 0, 100)}%"></div></div>
    </section>

    <section class="panel" style="margin-top:18px">
      <h2>Business goals</h2>
      ${rewardInsightMarkup(insight)}
    </section>

    <section class="grid two" style="margin-top:18px">
      <div class="panel">
        <h2>Active badges</h2>
        <div class="list-grid" style="grid-template-columns:1fr">
          ${badges.length ? badges.map(userBadgeCardMarkup).join('') : emptyMarkup('No active badge.')}
        </div>
      </div>
      <div class="panel">
        <h2>Notifications</h2>
        ${notificationsListMarkup(notifications)}
      </div>
    </section>

    <section class="panel" style="margin-top:18px">
      <h2>Badge PDFs</h2>
      ${certificateDownloadsMarkup(history)}
    </section>

    <section class="panel" style="margin-top:18px">
      <h2>Evaluation history</h2>
      ${evaluationsTableMarkup(relatedEvaluations, true)}
    </section>

    <section class="panel" style="margin-top:18px">
      <h2>Badge and reward history</h2>
      ${historyTableMarkup(history, true)}
    </section>
  `;

  wireCommonActions();
  qs('[data-action="refresh"]')?.addEventListener('click', () => navigate('space'));
  qs('[data-action="change-space-email"]')?.addEventListener('click', () => {
    state.userEmail = '';
    localStorage.removeItem(STORAGE_KEYS.userEmail);
    navigate('space');
  });
  wireHistoryActions();
}

async function renderSettings() {
  app.innerHTML = `
    ${pageHeader('Configuration', 'Settings')}
    <section class="split">
      <form id="settings-form" class="form-card">
        <h2>Services</h2>
        <div class="form-grid">
          ${inputField('evaluationBaseUrl', 'Evaluation API', state.config.evaluationBaseUrl, true, 'url')}
          ${inputField('rewardBaseUrl', 'Reward API', state.config.rewardBaseUrl, true, 'url')}
          ${inputField('userEmail', 'User email', state.userEmail || '', false, 'email')}
          <div class="form-actions">
            <button class="primary" type="submit">Save</button>
          </div>
        </div>
      </form>

      <div class="panel">
        <h2>Status</h2>
        <div class="grid two">
          ${metricCard('Frontend port', window.location.port || '4200', window.location.origin, 'teal')}
          ${metricCard('Local security', 'Open', 'Free access for local tests', 'gold')}
        </div>
        <div class="toolbar" style="margin-top:16px">
          <button class="secondary" type="button" data-action="test-evaluation">Test evaluation</button>
          <button class="secondary" type="button" data-action="test-reward">Test reward</button>
        </div>
        <div id="settings-result" class="result-box" style="margin-top:16px"></div>
      </div>
    </section>
  `;

  qs('#settings-form')?.addEventListener('submit', (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    saveConfig({
      evaluationBaseUrl: trimSlash(form.get('evaluationBaseUrl')),
      rewardBaseUrl: trimSlash(form.get('rewardBaseUrl'))
    });
    state.userEmail = String(form.get('userEmail') || '').trim();
    localStorage.setItem(STORAGE_KEYS.userEmail, state.userEmail);
    toast('Settings saved.', 'success');
    navigate('settings');
  });

  qs('[data-action="test-evaluation"]')?.addEventListener('click', () => testEndpoint('evaluation'));
  qs('[data-action="test-reward"]')?.addEventListener('click', () => testEndpoint('reward'));
}

function wireCommonActions() {
  qsa('[data-page-link]').forEach((button) => {
    button.addEventListener('click', () => navigate(button.dataset.pageLink));
  });
}

function wireEvaluationForm() {
  const form = qs('#evaluation-form');
  if (!form) return;

  const updatePreview = () => {
    qs('#score-preview').textContent = calculateCriteriaAverage(form).toFixed(2);
  };

  qsa('[data-criterion-input]', form).forEach((input) => {
    input.addEventListener('input', () => {
      const output = form.querySelector(`[data-criterion-output="${input.name}"]`);
      if (output) output.textContent = input.value;
      updatePreview();
    });
  });

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    await guardedAction(async () => {
      const formData = new FormData(form);
      const score = Math.round(calculateCriteriaAverage(form));
      const evaluatorName = String(formData.get('evaluatorName') || '').trim();
      const userEmail = String(formData.get('userEmail') || '').trim();

      const payload = {
        score,
        evaluatedUserEmail: String(formData.get('evaluatedUserEmail') || '').trim(),
        comment: String(formData.get('comment') || '').trim(),
        anonymous: formData.get('anonymous') === 'on',
        userEmail,
        projectName: String(formData.get('projectName') || '').trim(),
        evaluatorName,
        evaluatedUserName: String(formData.get('evaluatedUserName') || '').trim(),
        typeEvaluation: String(formData.get('typeEvaluation') || 'TECHNIQUE')
      };

      const saved = await evaluationApi.evaluations.create(payload);

      toast(`Evaluation #${saved?.id ?? ''} saved.`, 'success');
      state.userEmail = payload.userEmail || payload.evaluatedUserEmail;
      localStorage.setItem(STORAGE_KEYS.userEmail, state.userEmail);
      await navigate('evaluations');
    });
  });
}

function wireSyncForm() {
  const form = qs('#sync-form');
  if (!form) return;

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    await guardedAction(async () => {
      const formData = new FormData(form);
      const evaluatedAt = String(formData.get('evaluatedAt') || '').trim();
      const payload = {
        freelancerEmail: String(formData.get('freelancerEmail') || '').trim(),
        freelancerName: String(formData.get('freelancerName') || '').trim(),
        projectName: String(formData.get('projectName') || '').trim(),
        currentScore: toInt(formData.get('currentScore')),
        averageScore: toFloat(formData.get('averageScore')),
        totalPoints: toInt(formData.get('totalPoints')),
        totalEvaluations: toInt(formData.get('totalEvaluations')),
        positiveEvaluations: toInt(formData.get('positiveEvaluations')),
        completedProjects: toInt(formData.get('completedProjects')),
        evaluatedAt: evaluatedAt ? normalizeLocalDateTime(evaluatedAt) : normalizeLocalDateTime(localDateTimeValue())
      };

      const response = await recompenseApi.advanced.processEvaluation(payload);

      state.lastSyncResponse = response;
      const result = qs('#sync-result');
      result.classList.add('visible');
      result.innerHTML = rewardResponseMarkup(response);
      toast('Reward engine executed.', 'success');
    });
  });
}

function wireEvaluationDelete() {
  qsa('[data-delete-evaluation]').forEach((button) => {
    button.addEventListener('click', async () => {
      const confirmed = await confirmDialog('Delete this evaluation?', 'Confirm deletion');
      if (!confirmed) return;
      await guardedAction(async () => {
        await evaluationApi.evaluations.deleteById(button.dataset.deleteEvaluation);
        toast('Evaluation deleted.', 'success');
        await navigate('evaluations');
      });
    });
  });
}

function wireBadgeForm() {
  const form = qs('#badge-form');
  if (!form) return;

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    await guardedAction(async () => {
      const formData = new FormData(form);
      const conditionType = String(formData.get('conditionType') || 'AVERAGE_SCORE');
      const payload = {
        name: String(formData.get('name') || '').trim(),
        description: String(formData.get('description') || '').trim(),
        icon: String(formData.get('icon') || '').trim(),
        conditionType,
        conditionValue: toFloat(formData.get('conditionValue')),
        category: conditionType === 'POINTS' ? 'POINTS' : 'SCORE',
        pointsReward: toInt(formData.get('pointsReward')),
        autoAssignable: true,
        certificateEligible: formData.get('certificateEligible') === 'on',
        isActive: formData.get('isActive') === 'on'
      };

      const editingId = state.editingBadge?.id;
      if (editingId) {
        await recompenseApi.badges.update(editingId, payload);
      } else {
        await recompenseApi.badges.create(payload);
      }

      state.editingBadge = null;
      toast(editingId ? 'Badge updated.' : 'Badge created.', 'success');
      await navigate('badges');
    });
  });
}

function wireBadgeCards(badges) {
  qsa('[data-edit-badge]').forEach((button) => {
    button.addEventListener('click', () => {
      state.editingBadge = badges.find((badge) => String(badge.id) === button.dataset.editBadge) || null;
      navigate('badges');
    });
  });

  qsa('[data-delete-badge]').forEach((button) => {
    button.addEventListener('click', async () => {
      const confirmed = await confirmDialog('Delete this badge?', 'Confirm deletion');
      if (!confirmed) return;
      await guardedAction(async () => {
        await recompenseApi.badges.deleteById(button.dataset.deleteBadge);
        toast('Badge deleted.', 'success');
        await navigate('badges');
      });
    });
  });
}

function wireRewardForm() {
  const form = qs('#reward-form');
  if (!form) return;

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    await guardedAction(async () => {
      const formData = new FormData(form);
      const payload = {
        title: String(formData.get('title') || '').trim(),
        description: String(formData.get('description') || '').trim(),
        pointsRequired: toInt(formData.get('pointsRequired')),
        stock: toInt(formData.get('stock')),
        imageUrl: String(formData.get('imageUrl') || '').trim(),
        isActive: formData.get('isActive') === 'on'
      };

      const editingId = state.editingReward?.id;
      if (editingId) {
        await recompenseApi.rewards.update(editingId, payload);
      } else {
        await recompenseApi.rewards.create(payload);
      }

      state.editingReward = null;
      toast(editingId ? 'Reward updated.' : 'Reward created.', 'success');
      await navigate('recompenses');
    });
  });
}

function wireRewardCards(rewards) {
  qsa('[data-edit-reward]').forEach((button) => {
    button.addEventListener('click', () => {
      state.editingReward = rewards.find((reward) => String(reward.id) === button.dataset.editReward) || null;
      navigate('recompenses');
    });
  });

  qsa('[data-delete-reward]').forEach((button) => {
    button.addEventListener('click', async () => {
      const confirmed = await confirmDialog('Delete this reward?', 'Confirm deletion');
      if (!confirmed) return;
      await guardedAction(async () => {
        await recompenseApi.rewards.deleteById(button.dataset.deleteReward);
        toast('Reward deleted.', 'success');
        await navigate('recompenses');
      });
    });
  });
}

function wireHistoryActions() {
  qsa('[data-download-certificate]').forEach((button) => {
    button.addEventListener('click', async () => {
      await guardedAction(async () => {
        const historyId = button.dataset.downloadCertificate;
        const blob = await recompenseApi.history.downloadCertificate(historyId);
        downloadBlob(blob, `reward-certificate-${historyId}.pdf`);
        toast('Certificate downloaded.', 'success');
      });
    });
  });

  qsa('[data-resend-certificate]').forEach((button) => {
    button.addEventListener('click', async () => {
      const recipient = await promptDialog({
        title: 'Resend certificate',
        message: 'Recipient email',
        initialValue: button.dataset.userEmail || '',
        confirmLabel: 'Send'
      });
      if (recipient === null) return;

      await guardedAction(async () => {
        await recompenseApi.history.resendCertificateEmail(
          button.dataset.resendCertificate,
          recipient.trim()
        );
        toast('Email sent.', 'success');
      });
    });
  });
}

function wireSpaceEmailForm() {
  qs('#space-email-form')?.addEventListener('submit', (event) => {
    event.preventDefault();
    const email = String(new FormData(event.currentTarget).get('email') || '').trim();
    state.userEmail = email;
    localStorage.setItem(STORAGE_KEYS.userEmail, email);
    navigate('space');
  });
}

async function guardedAction(action) {
  try {
    await action();
  } catch (error) {
    toast(error.message, 'error');
  }
}

async function testEndpoint(service) {
  const result = qs('#settings-result');
  result.classList.add('visible');
  result.textContent = 'Testing...';

  try {
    const data = service === 'evaluation'
      ? await evaluationApi.evaluations.list()
      : await recompenseApi.dashboard();

    result.innerHTML = `<div class="code-box">${escapeHtml(JSON.stringify(data, null, 2))}</div>`;
  } catch (error) {
    result.innerHTML = alertMarkup(error.message, 'error');
  }
}

function metricCard(label, value, detail, tone = '') {
  return `
    <article class="metric-card ${tone}">
      <div class="label">${escapeHtml(label)}</div>
      <div class="value">${escapeHtml(value)}</div>
      <div class="detail">${escapeHtml(detail)}</div>
    </article>
  `;
}

function inputField(name, label, value = '', required = false, type = 'text') {
  return `
    <div class="field">
      <label for="${attr(name)}">${escapeHtml(label)}</label>
      <input id="${attr(name)}" name="${attr(name)}" type="${attr(type)}" value="${attr(value)}" ${required ? 'required' : ''} />
    </div>
  `;
}

function numberField(name, label, value = 0, min = 0, max = 100, step = 1) {
  return `
    <div class="field">
      <label for="${attr(name)}">${escapeHtml(label)}</label>
      <input id="${attr(name)}" name="${attr(name)}" type="number" value="${attr(value)}" min="${attr(min)}" max="${attr(max)}" step="${attr(step)}" required />
    </div>
  `;
}

function textareaField(name, label, value = '') {
  return `
    <div class="field full">
      <label for="${attr(name)}">${escapeHtml(label)}</label>
      <textarea id="${attr(name)}" name="${attr(name)}">${escapeHtml(value)}</textarea>
    </div>
  `;
}

function selectField(name, label, options, selected) {
  return `
    <div class="field">
      <label for="${attr(name)}">${escapeHtml(label)}</label>
      <select id="${attr(name)}" name="${attr(name)}">
        ${options.map((option) => `
          <option value="${attr(option)}" ${option === selected ? 'selected' : ''}>${escapeHtml(optionLabel(option))}</option>
        `).join('')}
      </select>
    </div>
  `;
}

function optionLabel(option) {
  return option === 'AUTRE' ? 'OTHER' : option;
}

function toggleField(name, label, checked) {
  return `
    <label class="toggle-field">
      <input type="checkbox" name="${attr(name)}" ${checked ? 'checked' : ''} />
      <span>${escapeHtml(label)}</span>
    </label>
  `;
}

function criterionField(name, label, value) {
  return `
    <label class="criterion">
      <span class="field-label">${escapeHtml(label)}</span>
      <input data-criterion-input type="range" name="${attr(name)}" min="1" max="5" step="1" value="${attr(value)}" />
      <output data-criterion-output="${attr(name)}">${escapeHtml(value)}</output>
    </label>
  `;
}

function badgeCardMarkup(badge) {
  return `
    <article class="list-card">
      <div class="list-card-header">
        <div class="medal ${badgeTone(badge)}">${escapeHtml(displayIcon(badge.icon, badge.name))}</div>
        <div>
          <p class="card-title">${escapeHtml(badge.name)}</p>
          <p class="card-meta">${escapeHtml(badge.description || 'No description')}</p>
        </div>
        ${statusPill(truthy(badge.isActive))}
      </div>
      <div class="stat-line">
        ${badgePill(badge.conditionType || badge.category)}
        <span>Threshold ${escapeHtml(badge.conditionValue ?? 0)}</span>
        <span>Automatic</span>
        <span>${truthy(badge.certificateEligible) ? 'Certificate' : 'No certificate'}</span>
      </div>
      <div class="row-actions">
        <button class="ghost icon-only" type="button" data-edit-badge="${attr(badge.id)}" title="Edit" aria-label="Edit">e</button>
        <button class="danger icon-only" type="button" data-delete-badge="${attr(badge.id)}" title="Delete" aria-label="Delete">x</button>
      </div>
    </article>
  `;
}

function rewardCardMarkup(reward) {
  const image = reward.imageUrl
    ? `<img class="reward-image" src="${attr(reward.imageUrl)}" alt="" onerror="this.classList.add('hidden')" />`
    : `<div class="medal gold">${escapeHtml(initials(reward.title))}</div>`;

  return `
    <article class="list-card">
      <div class="list-card-header">
        ${image}
        <div>
          <p class="card-title">${escapeHtml(reward.title)}</p>
          <p class="card-meta">${escapeHtml(reward.description || 'No description')}</p>
        </div>
        ${statusPill(truthy(reward.isActive))}
      </div>
      <div class="stat-line">
        <span>${escapeHtml(reward.pointsRequired ?? 0)} pts</span>
        <span>Stock ${escapeHtml(reward.stock ?? -1)}</span>
        <span>${formatDate(reward.createdAt)}</span>
      </div>
      <div class="row-actions">
        <button class="ghost icon-only" type="button" data-edit-reward="${attr(reward.id)}" title="Edit" aria-label="Edit">e</button>
        <button class="danger icon-only" type="button" data-delete-reward="${attr(reward.id)}" title="Delete" aria-label="Delete">x</button>
      </div>
    </article>
  `;
}

function userBadgeCardMarkup(badge) {
  return `
    <article class="list-card">
      <div class="list-card-header">
        <div class="medal ${badgeTone(badge)}">${escapeHtml(displayIcon(badge.icon, badge.badgeName))}</div>
        <div>
          <p class="card-title">${escapeHtml(badge.badgeName)}</p>
          <p class="card-meta">${escapeHtml(badge.statusReason || badge.description || '')}</p>
        </div>
        ${statusPill(Boolean(badge.active))}
      </div>
      <div class="stat-line">
        ${badgePill(badge.conditionType)}
        <span>Threshold ${escapeHtml(badge.conditionValue ?? 0)}</span>
        <span>${formatDate(badge.dateAssigned)}</span>
      </div>
    </article>
  `;
}

function evaluationsTableMarkup(evaluations, compact = false) {
  if (!evaluations.length) {
    return emptyMarkup('No evaluations found.');
  }

  return `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Project</th>
            <th>Freelancer</th>
            <th>Client</th>
            <th>Score</th>
            <th>Date</th>
            ${compact ? '' : '<th>Actions</th>'}
          </tr>
        </thead>
        <tbody>
          ${evaluations.map((evaluation) => `
            <tr>
              <td>#${escapeHtml(evaluation.id ?? '')}</td>
              <td>${escapeHtml(evaluation.projectName || '')}</td>
              <td>
                <strong>${escapeHtml(evaluation.evaluatedUserName || '')}</strong><br />
                <span class="muted">${escapeHtml(evaluation.evaluatedUserEmail || '')}</span>
              </td>
              <td>${escapeHtml(evaluation.anonymous ? 'Anonymous' : evaluation.evaluatorName || evaluation.userEmail || '')}</td>
              <td>${starsMarkup(evaluation.score)}</td>
              <td>${formatDate(evaluation.updatedAt || evaluation.date)}</td>
              ${compact ? '' : `
                <td>
                  <button class="danger icon-only" type="button" data-delete-evaluation="${attr(evaluation.id)}" title="Delete" aria-label="Delete">x</button>
                </td>
              `}
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function profilesTableMarkup(profiles, insightsByEmail = {}) {
  if (!profiles.length) {
    return emptyMarkup('No profile found.');
  }

  return `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Freelancer</th>
            <th>Score</th>
            <th>Points</th>
            <th>Level</th>
            <th>Badges</th>
            <th>Analysis</th>
            <th>Updated</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${profiles.map((profile) => {
            const insight = insightsByEmail[profile.userEmail] || {};
            return `
            <tr data-profile-row="${attr([
              profile.userEmail,
              profile.userName,
              profile.currentLevel,
              profile.currentScoreBadge,
              profile.currentPointsBadge,
              insight.performanceStatus,
              insight.nextRecompense
            ].join(' '))}">
              <td>
                <strong>${escapeHtml(profile.userName || profile.userEmail)}</strong><br />
                <span class="muted">${escapeHtml(profile.userEmail || '')}</span>
              </td>
              <td>${starsMarkup(profile.averageScore)}</td>
              <td>${escapeHtml(profile.totalPoints ?? 0)}</td>
              <td>${badgePill(profile.currentLevel || 'Level 1')}</td>
              <td>
                ${profile.currentScoreBadge ? badgePill(profile.currentScoreBadge) : ''}
                ${profile.currentPointsBadge ? badgePill(profile.currentPointsBadge) : ''}
              </td>
              <td>${profileInsightSummaryMarkup(insight)}</td>
              <td>${formatDate(profile.updatedAt)}</td>
              <td><button class="ghost" type="button" data-profile-email="${attr(profile.userEmail)}">Profile</button></td>
            </tr>
          `;
          }).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function profileInsightSummaryMarkup(insight) {
  if (!insight || !insight.userEmail) {
    return '<span class="muted">Not calculated</span>';
  }

  const nextGoal = insight.nextRecompense
    ? `${insight.nextRecompense} (${insight.pointsToNextRecompense ?? 0} pts)`
    : insight.nextPointsBadge
      ? `${insight.nextPointsBadge} (${insight.pointsToNextBadge ?? 0} pts)`
      : insight.nextScoreBadge
        ? `${insight.nextScoreBadge} (+${insight.scoreToNextBadge ?? 0})`
        : 'Goals reached';

  return `
    <div>
      ${badgePill(statusLabel(insight.performanceStatus))}
      <div class="muted" style="margin-top:6px">${escapeHtml(nextGoal)}</div>
    </div>
  `;
}

function rewardInsightMarkup(insight) {
  if (!insight) {
    return emptyMarkup('Business insight is not available for this freelancer.');
  }

  const recommendations = Array.isArray(insight.recommendations) ? insight.recommendations : [];
  const opportunities = Array.isArray(insight.opportunities) ? insight.opportunities.slice(0, 4) : [];

  return `
    <div class="grid four">
      ${metricCard('Status', statusLabel(insight.performanceStatus), insight.currentLevel || 'Current level', 'teal')}
      ${metricCard('Score badge', insight.nextScoreBadge || 'Complete', insight.nextScoreBadge ? `+${insight.scoreToNextBadge ?? 0} average` : 'No remaining threshold', 'gold')}
      ${metricCard('Points badge', insight.nextPointsBadge || 'Complete', insight.nextPointsBadge ? `${insight.pointsToNextBadge ?? 0} pts left` : 'No remaining threshold', 'rose')}
      ${metricCard('Reward', insight.nextRecompense || 'Complete', insight.nextRecompense ? `${insight.pointsToNextRecompense ?? 0} pts left` : `${insight.eligibleRecompensesCount ?? 0} eligible`, 'ink')}
    </div>

    <div class="grid two" style="margin-top:16px">
      <div>
        <h3>Recommendations</h3>
        ${recommendations.length ? `
          <div class="grid">
            ${recommendations.map((recommendation) => `
              <article class="list-card" style="min-height:auto">
                <p class="card-title">${escapeHtml(recommendation)}</p>
              </article>
            `).join('')}
          </div>
        ` : emptyMarkup('No recommendation.')}
      </div>
      <div>
        <h3>Reward opportunities</h3>
        ${opportunities.length ? `
          <div class="grid">
            ${opportunities.map(opportunityCardMarkup).join('')}
          </div>
        ` : emptyMarkup('No active reward.')}
      </div>
    </div>
  `;
}

function opportunityCardMarkup(opportunity) {
  const stateText = opportunity.alreadyAwarded
    ? 'Already assigned'
    : opportunity.eligible
      ? 'Eligible now'
      : opportunity.available
        ? `${opportunity.remainingValue ?? 0} pts left`
        : 'Out of stock';

  return `
    <article class="list-card" style="min-height:auto">
      <p class="card-title">${escapeHtml(opportunity.title || 'Reward')}</p>
      <p class="card-meta">${escapeHtml(stateText)}</p>
    </article>
  `;
}

function certificateDownloadsMarkup(history) {
  const certificates = history.filter((item) => item.certificateGenerated);
  if (!certificates.length) {
    return emptyMarkup('No PDF certificate available.');
  }

  return `
    <div class="grid two">
      ${certificates.map((item) => `
        <article class="list-card" style="min-height:auto">
          <div>
            <p class="card-title">${escapeHtml(item.rewardName || 'Certificate')}</p>
            <p class="card-meta">${escapeHtml(formatDate(item.eventDate))}</p>
          </div>
          <button class="ghost" type="button" data-download-certificate="${attr(item.id)}">Download PDF</button>
        </article>
      `).join('')}
    </div>
  `;
}

function historyTableMarkup(history, compact = false) {
  if (!history.length) {
    return emptyMarkup('No event found.');
  }

  return `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Freelancer</th>
            <th>Reward</th>
            <th>Action</th>
            <th>Snapshots</th>
            ${compact ? '<th>PDF</th>' : '<th>Email</th>'}
          </tr>
        </thead>
        <tbody>
          ${history.map((item) => `
            <tr>
              <td>${formatDate(item.eventDate)}</td>
              <td>
                <strong>${escapeHtml(item.userName || item.userEmail)}</strong><br />
                <span class="muted">${escapeHtml(item.userEmail || '')}</span>
              </td>
              <td>
                ${badgePill(item.rewardType)}
                <div>${escapeHtml(item.rewardName || '')}</div>
                <small class="muted">${escapeHtml(item.description || '')}</small>
              </td>
              <td>${badgePill(item.actionType)}</td>
              <td>
                <span>${escapeHtml(item.averageScoreSnapshot ?? 0)} / 5</span><br />
                <span class="muted">${escapeHtml(item.totalPointsSnapshot ?? 0)} pts</span>
              </td>
              <td>
                <div class="inline-actions">
                  ${compact && item.certificateGenerated ? `<button class="ghost icon-only" type="button" data-download-certificate="${attr(item.id)}" title="PDF certificate" aria-label="PDF certificate">v</button>` : ''}
                  ${compact ? '' : `<button class="ghost icon-only" type="button" data-resend-certificate="${attr(item.id)}" data-user-email="${attr(item.userEmail)}" title="Resend email" aria-label="Resend email">@</button>`}
                </div>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function topFreelancersMarkup(freelancers) {
  if (!freelancers.length) {
    return emptyMarkup('No synced freelancer.');
  }

  return `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Freelancer</th>
            <th>Score</th>
            <th>Points</th>
            <th>Level</th>
          </tr>
        </thead>
        <tbody>
          ${freelancers.map((freelancer) => `
            <tr>
              <td>
                <strong>${escapeHtml(freelancer.userName || freelancer.userEmail)}</strong><br />
                <span class="muted">${escapeHtml(freelancer.userEmail || '')}</span>
              </td>
              <td>${starsMarkup(freelancer.averageScore)}</td>
              <td>${escapeHtml(freelancer.totalPoints ?? 0)}</td>
              <td>${badgePill(freelancer.currentLevel || 'Level 1')}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function monthlyProgressMarkup(rows) {
  if (!rows.length) {
    return emptyMarkup('No monthly progress yet.');
  }

  const max = Math.max(...rows.map((row) => Number(row.awardedCount || 0) + Number(row.revokedCount || 0)), 1);
  return `
    <div class="monthly-bars">
      ${rows.map((row) => {
        const awarded = Number(row.awardedCount || 0);
        const revoked = Number(row.revokedCount || 0);
        const value = ((awarded + revoked) / max) * 100;
        return `
          <div class="monthly-row">
            <strong>${escapeHtml(row.month)}</strong>
            <div class="progress-track"><div class="progress-bar" style="--value:${value}%"></div></div>
            <span class="muted">${awarded} / ${revoked}</span>
          </div>
        `;
      }).join('')}
    </div>
  `;
}

function notificationsListMarkup(notifications) {
  if (!notifications.length) {
    return emptyMarkup('No notification.');
  }

  return `
    <div class="grid">
      ${notifications.map((notification) => `
        <article class="list-card" style="min-height:auto">
          <p class="card-title">${escapeHtml(notification.message || '')}</p>
          <p class="card-meta">${formatDate(notification.createdAt)}</p>
        </article>
      `).join('')}
    </div>
  `;
}

function simpleListMarkup(items, emptyText) {
  if (!items.length) {
    return emptyMarkup(emptyText);
  }

  return `
    <div class="grid">
      ${items.map((item) => `
        <article class="list-card" style="min-height:auto">
          <p class="card-title">${escapeHtml(item)}</p>
        </article>
      `).join('')}
    </div>
  `;
}

function rewardResponseMarkup(response) {
  return `
    <div class="grid two">
      ${metricCard('Score badge', response.currentScoreBadge || 'None', `${response.averageScore ?? 0} / 5`, 'teal')}
      ${metricCard('Points badge', response.currentPointsBadge || 'None', `${response.totalPoints ?? 0} pts`, 'gold')}
      ${metricCard('Level', response.currentLevel || 'N/A', response.freelancerEmail || '', 'rose')}
      ${metricCard('Message', response.message || 'OK', response.freelancerName || '', 'ink')}
    </div>
  `;
}

function alertMarkup(message, type = '') {
  return `<div class="alert ${type}">${escapeHtml(message)}</div>`;
}

function emptyMarkup(message) {
  return `<div class="empty-state">${escapeHtml(message)}</div>`;
}

function statusPill(active) {
  return `<span class="status-pill ${active ? 'active' : 'inactive'}">${active ? 'Active' : 'Inactive'}</span>`;
}

function badgePill(value) {
  const text = String(value || 'N/A');
  let tone = 'custom';
  if (/score|average|professionnel|expert|debutant|intermediaire/i.test(text)) tone = 'score';
  if (/point|bronze|silver|gold|platinum/i.test(text)) tone = 'points';
  if (/niveau|level|upgraded|downgraded/i.test(text)) tone = 'level';
  return `<span class="badge-pill ${tone}">${escapeHtml(text)}</span>`;
}

function statusLabel(status) {
  return ({
    WAITING_FOR_EVALUATIONS: 'Waiting',
    ELITE_READY: 'Elite',
    PREMIUM_PROGRESS: 'Premium',
    NEEDS_ATTENTION: 'Needs attention',
    PROGRESSING: 'In progress'
  })[status] || 'In progress';
}

function starsMarkup(score) {
  const rounded = Math.max(0, Math.min(5, Number(score || 0)));
  const full = Math.round(rounded);
  return `<span title="${attr(rounded)} / 5">${'*'.repeat(full)}${'<span class="muted">*</span>'.repeat(5 - full)} <strong>${escapeHtml(rounded.toFixed(1))}</strong></span>`;
}

function loadingMarkup() {
  return '<div class="loader">Loading...</div>';
}

function renderFatalError(error) {
  app.innerHTML = `
    ${pageHeader('Error', 'Unable to load')}
    ${alertMarkup(error.message, 'error')}
  `;
}

function toast(message, type = 'success') {
  const element = document.createElement('div');
  element.className = `toast ${type}`;
  element.textContent = message;
  toastRegion.appendChild(element);
  window.setTimeout(() => element.remove(), 5200);
}

function firstError(...results) {
  const failed = results.find((result) => result && !result.ok);
  return failed?.error || null;
}

function collectCriteria(form) {
  return qsa('[data-criterion-input]', form).map((input) => ({
    label: input.closest('.criterion')?.querySelector('.field-label')?.textContent || input.name,
    score: toInt(input.value)
  }));
}

function calculateCriteriaAverage(form) {
  const criteria = collectCriteria(form);
  if (!criteria.length) return 0;
  return criteria.reduce((sum, criterion) => sum + criterion.score, 0) / criteria.length;
}

function averageEvaluationScore(evaluations) {
  const scores = evaluations.map((evaluation) => Number(evaluation.score)).filter(Number.isFinite);
  if (!scores.length) return '0.0';
  return (scores.reduce((sum, score) => sum + score, 0) / scores.length).toFixed(1);
}

function filterEvaluationsForUser(evaluations, email) {
  const target = String(email || '').trim().toLowerCase();
  if (!target) return [];

  return evaluations
    .filter((evaluation) => [
      evaluation.userEmail,
      evaluation.evaluatorEmail,
      evaluation.evaluatedUserEmail,
      evaluation.freelancerEmail
    ].some((value) => String(value || '').trim().toLowerCase() === target))
    .reverse();
}

function filterRows(query, selector) {
  const normalized = query.trim().toLowerCase();
  qsa(selector).forEach((row) => {
    row.classList.toggle('hidden', normalized && !row.dataset.profileRow.toLowerCase().includes(normalized));
  });
}

function downloadBlob(blob, fileName) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

function formatDate(value) {
  if (!value) return 'N/A';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

function localDateTimeValue(date = new Date()) {
  const offset = date.getTimezoneOffset();
  const local = new Date(date.getTime() - offset * 60_000);
  return local.toISOString().slice(0, 16);
}

function normalizeLocalDateTime(value) {
  return value.length === 16 ? `${value}:00` : value;
}

function trimSlash(value) {
  return String(value || '').trim().replace(/\/+$/, '');
}

function displayIcon(icon, fallback) {
  const cleaned = String(icon || '').trim();
  if (cleaned) return cleaned.slice(0, 2);
  return initials(fallback || 'R');
}

function initials(value) {
  return String(value || 'R')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() || '')
    .join('') || 'R';
}

function badgeTone(badge) {
  const text = `${badge.conditionType || ''} ${badge.category || ''} ${badge.badgeName || ''} ${badge.name || ''}`;
  if (/point|bronze|silver|gold|platinum/i.test(text)) return 'gold';
  if (/level|niveau/i.test(text)) return 'rose';
  return '';
}

function sumBy(items, key) {
  return items.reduce((sum, item) => sum + Number(item[key] || 0), 0);
}

function truthy(value) {
  return value === true || value === 'true' || value === 1;
}

function toInt(value) {
  const parsed = Number.parseInt(value, 10);
  return Number.isFinite(parsed) ? parsed : 0;
}

function toFloat(value) {
  const parsed = Number.parseFloat(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, Number(value) || 0));
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  })[char]);
}

function attr(value) {
  return escapeHtml(value).replace(/`/g, '&#096;');
}

function qs(selector, root = document) {
  return root.querySelector(selector);
}

function qsa(selector, root = document) {
  return Array.from(root.querySelectorAll(selector));
}

}

