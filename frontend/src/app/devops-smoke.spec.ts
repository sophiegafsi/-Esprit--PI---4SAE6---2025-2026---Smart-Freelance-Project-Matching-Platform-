import { buildPipelineLabel } from './devops-ci.util';

describe('frontend CI', () => {
  it('runs unit tests in the pipeline', () => {
    expect(buildPipelineLabel('Frontend')).toBe('ci-frontend');
  });
});
