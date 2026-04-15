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

export function createReviewApi(request: ServiceRequest) {
  return {
    list(): Promise<any[]> {
      return request('evaluation', '/review/all');
    },
    getById(id: number | string): Promise<any> {
      return request('evaluation', `/review/${id}`);
    },
    getByUser(email: string): Promise<any[]> {
      return request('evaluation', `/review/user/${encodeURIComponent(email)}`);
    },
    getByEvaluation(evaluationId: number | string): Promise<any[]> {
      return request('evaluation', `/review/evaluation/${evaluationId}`);
    },
    add(payload: Record<string, unknown>): Promise<any> {
      return request('evaluation', '/review/add', {
        method: 'POST',
        body: payload
      });
    },
    update(id: number | string, payload: Record<string, unknown>): Promise<any> {
      return request('evaluation', `/review/update/${id}`, {
        method: 'PUT',
        body: payload
      });
    },
    deleteById(id: number | string): Promise<void> {
      return request('evaluation', `/review/delete/${id}`, {
        method: 'DELETE'
      });
    },
    advanced: {
      sentimentStats(): Promise<Record<string, number>> {
        return request('evaluation', '/review/sentiment-stats');
      },
      analyzeSentiment(text: string): Promise<{ text: string; sentiment: string }> {
        return request('evaluation', '/review/sentiment/analyze', {
          method: 'POST',
          body: { text }
        });
      },
      exportHistoryPdf(historyId: number | string): Promise<Blob> {
        return request('reward', `/api/rewards/certificates/${historyId}`, {
          responseType: 'blob'
        });
      }
    }
  };
}
