let stylesInjected = false;

type PromptOptions = {
  title?: string;
  message?: string;
  initialValue?: string;
  placeholder?: string;
  confirmLabel?: string;
  cancelLabel?: string;
};

function ensureStyles(): void {
  if (stylesInjected || typeof document === 'undefined') return;

  const style = document.createElement('style');
  style.textContent = `
    .app-dialog-overlay {
      position: fixed;
      inset: 0;
      background: rgba(2, 10, 28, 0.65);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 16px;
      z-index: 12000;
      backdrop-filter: blur(2px);
    }
    .app-dialog {
      width: min(460px, 100%);
      border-radius: 12px;
      border: 1px solid rgba(123, 160, 255, 0.32);
      background: #081328;
      color: #e8eefc;
      box-shadow: 0 24px 52px rgba(1, 6, 20, 0.45);
      padding: 18px;
      font-family: Inter, Segoe UI, Roboto, Arial, sans-serif;
    }
    .app-dialog h3 {
      margin: 0 0 10px;
      font-size: 1rem;
      font-weight: 700;
    }
    .app-dialog p {
      margin: 0 0 14px;
      color: #b8c6df;
      line-height: 1.4;
    }
    .app-dialog input {
      width: 100%;
      box-sizing: border-box;
      margin-bottom: 14px;
      border-radius: 10px;
      border: 1px solid rgba(123, 160, 255, 0.32);
      background: rgba(255, 255, 255, 0.04);
      color: #e8eefc;
      padding: 10px 12px;
      outline: none;
    }
    .app-dialog input:focus {
      border-color: rgba(96, 196, 255, 0.9);
      box-shadow: 0 0 0 3px rgba(96, 196, 255, 0.2);
    }
    .app-dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
    .app-dialog-actions button {
      border-radius: 10px;
      border: 1px solid rgba(123, 160, 255, 0.32);
      padding: 8px 14px;
      cursor: pointer;
      font-weight: 600;
    }
    .app-dialog-actions .cancel-btn {
      background: transparent;
      color: #dce7ff;
    }
    .app-dialog-actions .confirm-btn {
      border: none;
      background: linear-gradient(120deg, #ff8a3d, #ff4d79);
      color: #fff;
    }
  `;

  document.head.appendChild(style);
  stylesInjected = true;
}

function createDialogShell(title: string, message: string): {
  overlay: HTMLDivElement;
  panel: HTMLDivElement;
  actions: HTMLDivElement;
} {
  ensureStyles();

  const overlay = document.createElement('div');
  overlay.className = 'app-dialog-overlay';
  overlay.setAttribute('role', 'dialog');
  overlay.setAttribute('aria-modal', 'true');

  const panel = document.createElement('div');
  panel.className = 'app-dialog';
  panel.tabIndex = -1;

  const titleEl = document.createElement('h3');
  titleEl.textContent = title;
  panel.appendChild(titleEl);

  if (message) {
    const messageEl = document.createElement('p');
    messageEl.textContent = message;
    panel.appendChild(messageEl);
  }

  const actions = document.createElement('div');
  actions.className = 'app-dialog-actions';

  panel.appendChild(actions);
  overlay.appendChild(panel);

  return { overlay, panel, actions };
}

export function confirmDialog(
  message: string,
  title = 'Please confirm',
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel'
): Promise<boolean> {
  return new Promise((resolve) => {
    if (typeof document === 'undefined') {
      resolve(false);
      return;
    }

    const { overlay, panel, actions } = createDialogShell(title, message);

    const cancelBtn = document.createElement('button');
    cancelBtn.className = 'cancel-btn';
    cancelBtn.type = 'button';
    cancelBtn.textContent = cancelLabel;

    const confirmBtn = document.createElement('button');
    confirmBtn.className = 'confirm-btn';
    confirmBtn.type = 'button';
    confirmBtn.textContent = confirmLabel;

    const cleanup = (result: boolean) => {
      document.removeEventListener('keydown', onKeyDown);
      overlay.remove();
      resolve(result);
    };

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') cleanup(false);
      if (event.key === 'Enter') cleanup(true);
    };

    cancelBtn.addEventListener('click', () => cleanup(false));
    confirmBtn.addEventListener('click', () => cleanup(true));
    overlay.addEventListener('click', (event) => {
      if (event.target === overlay) cleanup(false);
    });

    actions.append(cancelBtn, confirmBtn);
    document.addEventListener('keydown', onKeyDown);
    document.body.appendChild(overlay);
    panel.focus();
    confirmBtn.focus();
  });
}

export function promptDialog(options: PromptOptions = {}): Promise<string | null> {
  const {
    title = 'Please confirm',
    message = '',
    initialValue = '',
    placeholder = '',
    confirmLabel = 'Save',
    cancelLabel = 'Cancel'
  } = options;

  return new Promise((resolve) => {
    if (typeof document === 'undefined') {
      resolve(null);
      return;
    }

    const { overlay, panel, actions } = createDialogShell(title, message);

    const input = document.createElement('input');
    input.type = 'email';
    input.value = initialValue;
    input.placeholder = placeholder;
    panel.insertBefore(input, actions);

    const cancelBtn = document.createElement('button');
    cancelBtn.className = 'cancel-btn';
    cancelBtn.type = 'button';
    cancelBtn.textContent = cancelLabel;

    const confirmBtn = document.createElement('button');
    confirmBtn.className = 'confirm-btn';
    confirmBtn.type = 'button';
    confirmBtn.textContent = confirmLabel;

    const cleanup = (result: string | null) => {
      document.removeEventListener('keydown', onKeyDown);
      overlay.remove();
      resolve(result);
    };

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') cleanup(null);
      if (event.key === 'Enter') cleanup(input.value.trim());
    };

    cancelBtn.addEventListener('click', () => cleanup(null));
    confirmBtn.addEventListener('click', () => cleanup(input.value.trim()));
    overlay.addEventListener('click', (event) => {
      if (event.target === overlay) cleanup(null);
    });

    actions.append(cancelBtn, confirmBtn);
    document.addEventListener('keydown', onKeyDown);
    document.body.appendChild(overlay);
    panel.focus();
    input.focus();
    input.select();
  });
}
