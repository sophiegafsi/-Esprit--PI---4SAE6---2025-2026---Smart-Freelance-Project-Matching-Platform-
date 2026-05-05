export function confirmDialog(title: string, text: string, icon: any = 'warning', confirmButtonText: string = 'Yes') {
    // Fallback to native confirm if sweetalert2 is omitted from the base project.
    return Promise.resolve({
        isConfirmed: window.confirm(`${title}\n\n${text}`)
    });
}
