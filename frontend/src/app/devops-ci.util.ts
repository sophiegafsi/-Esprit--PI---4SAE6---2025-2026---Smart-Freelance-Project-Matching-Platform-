export function buildPipelineLabel(scope: string): string {
  return `ci-${scope.trim().toLowerCase()}`;
}
