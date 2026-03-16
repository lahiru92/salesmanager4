// document.body.addEventListener('htmx:afterSwap', function(event) {
//     // Check if the swapped element is a toast (or part of the toast container)
//     if (event.detail.target.id === 'toast-container' || event.detail.target.closest('#toast-container')) {
//         // Find all uninitialized toasts within the container
//         const toastElements = document.querySelectorAll('.toast:not(.showing, .show)');
        
//         toastElements.forEach(toastEl => {

//             console.log('Initializing toast:', toastEl); // Debug log to check the toast element

//             // Initialize each new toast
//             const toast = new bootstrap.Toast(toastEl);
//             toast.show();

//             // Optional: Remove the toast element from the DOM after it is hidden
//             toastEl.addEventListener('hidden.bs.toast', function () {
//                 toastEl.remove();
//             });
//         });
//     }
// });

document.body.addEventListener("showToast", function (evt) {
    let bgColor = "#333333"; 
    let textColor = "#FFFFFF"; 

    switch (evt.detail.type) {
        case "info":
        case "success":
            bgColor = "#2196F3";
            break;
        case "warning":
            bgColor = "#FF9800";
            textColor = "#000000"; 
            break;
        case "error":
            bgColor = "#F44336";
            break;
    }

    Toastify({
        text: evt.detail.message,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: {
            background: bgColor,
            color: textColor
        }
    }).showToast();
    
});