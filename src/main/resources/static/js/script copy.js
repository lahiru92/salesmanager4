// Global tom select instance
let tsInstance = null;


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


function purchaseOrder() {
    return {
        items: [{"itemId": null, "name": "", "qty": "", "price": "", "uid": Date.now()}],
        errors: {
            items: [],
            form: null
        },
        validate() {
            this.errors.items = [];

            this.items.forEach((item, index) => {
                let rowErrors = {};

                if (!item.itemId) {
                    rowErrors.itemId = "Item is required";
                }

                if (!item.qty || item.qty <= 0) {
                    rowErrors.qty = "Qty must be > 0";
                }

                if (item.price == null || item.price < 0 || item.price === '') {
                    rowErrors.price = "Invalid price";
                }

                this.errors.items[index] = rowErrors;
            });

            // Remove completely empty rows from validation
            this.errors.items = this.errors.items.map((err, i) => {
                let item = this.items[i];
                if (!item.itemId && !item.qty && !item.price) {
                    return {}; // ignore blank row
                }
                return err;
            });

            // Check if any errors exist
            return this.errors.items.every(e => Object.keys(e).length === 0);
        },
        addRow() {
            this.items.push({
                uid: Date.now() + Math.random(),
                itemId: null,
                name: '',
                qty: 1,
                price: 0
            });
        },

        removeItem(index) {
            this.items.splice(index, 1);
        },

        initTomSelect(el, index) {
            new TomSelect(el, {
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
                    let selected = el.tomselect.options[value];

                    // 🚫 Prevent duplicates
                    if (this.items.some(i => i.itemId == value)) {
                        alert("Item already added");
                        el.tomselect.clear();
                        return;
                    }

                    this.items[index].itemId = value;
                    this.items[index].name = selected.name;
                    this.items[index].price = selected.price || 0;
                }
            });
        },

        cleanItems() {
            return this.items
                .filter(i => i.itemId) // keep only valid rows
                .map(i => ({
                    itemId: i.itemId,
                    qty: i.qty,
                    price: i.price
                }));
        },
        handleHtmxSubmit(e) {
            const validItems = this.validate();
            if (this.cleanItems().length === 0 ) {
                console.log("No items to submit");
                this.errors.form = "Please add at least one item";
                e.preventDefault();
                return ;
            }

            if (!validItems) {
                console.log("Validation failed", this.errors);
                this.errors.form = "Please fix errors before submitting";
                e.preventDefault();
                return ;
            }

        },
        get total() {
            return this.items.reduce((sum, i) => sum + (i.qty * i.price), 0);
        }
    }
}



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

// 1. Run on initial page load
document.addEventListener('DOMContentLoaded', initProductSearch);

// 2. Run after EVERY HTMX swap
document.body.addEventListener('htmx:afterSettle', function(evt) {
    // We re-run the init. If the element #f-id is in the new HTML, 
    // it gets a fresh Tom Select.
    initProductSearch();
    
    // Also recalculate totals in case the server returned existing rows
    reIndexAndTotal(); 
});