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
        items: [],

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
        handleSubmit(e) {
            if (this.cleanItems().length === 0) {
                alert("Please add at least one item");
                return;
            }

            e.target.submit(); // continue submission
        },
        get total() {
            return this.items.reduce((sum, i) => sum + (i.qty * i.price), 0);
        }
    }
}
