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

export function createEvaluationApi(request: ServiceRequest) {
  return {
    evaluations: {
      list(): Promise<any[]> {
        return request('evaluation', '/evaluations/all');
      },
      getById(id: number | string): Promise<any> {
        return request('evaluation', `/evaluations/${id}`);
      },
      getByUser(email: string): Promise<any[]> {
        return request('evaluation', `/evaluations/user/${encodeURIComponent(email)}`);
      },
      create(payload: Record<string, unknown>): Promise<any> {
        return request('evaluation', '/evaluations/add', {
          method: 'POST',
          body: payload
        });
      },
      update(id: number | string, payload: Record<string, unknown>): Promise<any> {
        return request('evaluation', `/evaluations/update/${id}`, {
          method: 'PUT',
          body: payload
        });
      },
      deleteById(id: number | string): Promise<void> {
        return request('evaluation', `/evaluations/delete/${id}`, {
          method: 'DELETE'
        });
      },
      average(userName: string): Promise<number> {
        return request('evaluation', `/evaluations/average/${encodeURIComponent(userName)}`);
      }
    },
    sentiment: {
      stats(): Promise<Record<string, number>> {
        return request('evaluation', '/review/sentiment-stats');
      }
    },
    advanced: {
      analyzeSentiment(text: string): Promise<{ text: string; sentiment: string }> {
        return request('evaluation', '/review/sentiment/analyze', {
          method: 'POST',
          body: { text }
        });
      }
    }
  };
}
