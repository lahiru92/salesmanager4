let tsInstance, supplierTsInstance;

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


// *************************************************
// Initialize a tom select component for item search
// This function handles the Tom Select setup
function initProductSearch() {
    const el = document.getElementById('f-id');
    if (!el) return;

    // Destroy existing instance if it exists (prevents memory leaks/ghost elements)
    if (el.tomselect) {
        el.tomselect.destroy();
    }

    tsInstance = new TomSelect(el, {
        valueField: 'itemId',
        labelField: 'name',
        searchField: 'name',
        load: function(query, callback) {
            if (!query.length) return callback();
            fetch(`/items/api/list?q=${encodeURIComponent(query)}`)
                .then(res => res.json())
                .then(json => callback(json))
                .catch(() => callback());
        },
        onChange: function(val) {
            const data = this.options[val];
            if (data) {
                // document.getElementById('f-price').value = data.price;
                // updateFooterSubtotal();
            }
        }
    });
}

// // 1. Run on initial page load
// document.addEventListener('DOMContentLoaded', initProductSearch);

// // 2. Run after EVERY HTMX swap
// document.body.addEventListener('htmx:afterSettle', function(evt) {
//     // We re-run the init. If the element #f-id is in the new HTML, 
//     // it gets a fresh Tom Select.
//     initProductSearch();
    
//     // Also recalculate totals in case the server returned existing rows
//     reIndexAndTotal(); 
// });


document.addEventListener('htmx:afterSettle', function(evt) {
    // console.log(evt.target.querySelector('#content .purchase-order'));

    if (evt.target.querySelector('#content .purchase-order')) {
        const itemEl = document.getElementById('f-id');
        if (itemEl) {
            if (itemEl.tomselect) {
                itemEl.tomselect.destroy();
            }
            
            tsInstance = new TomSelect('#f-id', {
                valueField: 'itemId',
                labelField: 'name',
                searchField: ['name'],

                load: function(query, callback) {
                    if (!query.length) return callback();

                    fetch('/items/api/list?q=' + query)
                        .then(res => res.json())
                        .then(json => callback(json))
                        .catch(() => callback());
                },

                onChange: (value) => {
                    console.log(value)
                }
            });
        }

        const supplierEl = document.getElementById('supplier');
        if (supplierEl) {
            if (supplierEl.tomselect) {
                supplierEl.tomselect.destroy();
            }
            
            supplierTsInstance = new TomSelect('#supplier', {
                valueField: 'supplierId',
                labelField: 'name',
                searchField: ['name'],

                load: function(query, callback) {
                    if (!query.length) return callback();

                    fetch('/suppliers/api/list?q=' + query)
                        .then(res => res.json())
                        .then(json => callback(json))
                        .catch(() => callback());
                },

                onChange: (value) => {
                    console.log(value)
                }
            });
        }
    }
    
});

// document.addEventListener('click', function(evt) {
//     console.log(evt.target);
    
// });