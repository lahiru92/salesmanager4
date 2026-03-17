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

// document.addEventListener("htmx:confirm", function(e) {
//     // The event is triggered on every trigger for a request, so we need to check if the element
//     // that triggered the request has a confirm question set via the hx-confirm attribute,
//     // if not we can return early and let the default behavior happen
//     if (!e.detail.question) return

//     // This will prevent the request from being issued to later manually issue it
//     e.preventDefault()

//     Swal.fire({
//         title: "Proceed?",
//         theme: 'bootstrap-5',
//         text: `${e.detail.question}`
//     }).then(function (result) {
//         if (result.isConfirmed) {
//             // If the user confirms, we manually issue the request
//             e.detail.issueRequest(true); // true to skip the built-in window.confirm()
//         }
//     })
// })