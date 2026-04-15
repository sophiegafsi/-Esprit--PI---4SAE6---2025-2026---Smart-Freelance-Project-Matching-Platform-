type RequestOptions = {
  method?: string;
  headers?: Record<string, string>;
  body?: unknown;
  responseType?: 'blob' | 'text';
};

type ServiceRequest = (
  service: 'evaluation' | 'reward',
  path: string,
  options?: RequestOptions
) => Promise<any>;

function encode(email: string): string {
  return encodeURIComponent(String(email || '').trim());
}

export function createBadgeApi(request: ServiceRequest) {
  return {
    dashboard(): Promise<any> {
      return request('reward', '/api/rewards/dashboard');
    },
    badges: {
      list(): Promise<any[]> {
        return request('reward', '/api/badges');
      },
      getById(id: number | string): Promise<any> {
        return request('reward', `/api/badges/${id}`);
      },
      listActive(): Promise<any[]> {
        return request('reward', '/api/badges/active');
      },
      create(payload: Record<string, unknown>): Promise<any> {
        return request('reward', '/api/badges', { method: 'POST', body: payload });
      },
      update(id: number | string, payload: Record<string, unknown>): Promise<any> {
        return request('reward', `/api/badges/${id}`, { method: 'PUT', body: payload });
      },
      deleteById(id: number | string): Promise<void> {
        return request('reward', `/api/badges/${id}`, { method: 'DELETE' });
      },
      activeForUser(email: string): Promise<any[]> {
        return request('reward', `/api/user-badges/active/${encode(email)}`);
      }
    },
    rewards: {
      list(): Promise<any[]> {
        return request('reward', '/api/recompenses');
      },
      getById(id: number | string): Promise<any> {
        return request('reward', `/api/recompenses/${id}`);
      },
      create(payload: Record<string, unknown>): Promise<any> {
        return request('reward', '/api/recompenses', { method: 'POST', body: payload });
      },
      update(id: number | string, payload: Record<string, unknown>): Promise<any> {
        return request('reward', `/api/recompenses/${id}`, { method: 'PUT', body: payload });
      },
      deleteById(id: number | string): Promise<void> {
        return request('reward', `/api/recompenses/${id}`, { method: 'DELETE' });
      }
    },
    profiles: {
      list(): Promise<any[]> {
        return request('reward', '/api/rewards/profiles');
      },
      getByEmail(email: string): Promise<any> {
        return request('reward', `/api/rewards/profiles/${encode(email)}`);
      }
    },
    insights: {
      list(): Promise<any[]> {
        return request('reward', '/api/rewards/insights');
      },
      byEmail(email: string): Promise<any> {
        return request('reward', `/api/rewards/insights/${encode(email)}`);
      }
    },
    history: {
      list(email?: string): Promise<any[]> {
        const query = email?.trim() ? `?email=${encode(email)}` : '';
        return request('reward', `/api/rewards/history${query}`);
      },
      downloadCertificate(historyId: number | string): Promise<Blob> {
        return request('reward', `/api/rewards/certificates/${historyId}`, {
          responseType: 'blob'
        });
      },
      resendCertificateEmail(historyId: number | string, recipientEmail?: string): Promise<string> {
        const query = recipientEmail?.trim()
          ? `?recipientEmail=${encode(recipientEmail)}`
          : '';
        return request('reward', `/api/rewards/certificates/${historyId}/resend-email${query}`, {
          method: 'POST',
          responseType: 'text'
        });
      }
    },
    points: {
      byUser(email: string): Promise<number> {
        return request('reward', `/api/points/points/${encode(email)}`);
      },
      progress(email: string): Promise<number> {
        return request('reward', `/api/points/points/progress/${encode(email)}`);
      }
    },
    notifications: {
      byUser(email: string): Promise<any[]> {
        return request('reward', `/api/notifications/${encode(email)}`);
      }
    },
    advanced: {
      processEvaluation(payload: Record<string, unknown>): Promise<any> {
        return request('reward', '/api/rewards/process-evaluation', {
          method: 'POST',
          body: payload
        });
      },
      assignEligibleRewards(): Promise<{ assignedRewards?: number }> {
        return request('reward', '/api/rewards/assign-pending-rewards', {
          method: 'POST'
        });
      },
      recalculateLevels(): Promise<{ message?: string }> {
        return request('reward', '/api/rewards/recalculate-levels', {
          method: 'POST'
        });
      }
    }
  };
}
