function updateFooterSubtotal() {
    const q = parseFloat(document.getElementById('f-qty').value) || 0;
    const p = parseFloat(document.getElementById('f-price').value) || 0;
    document.getElementById('f-subtotal').innerText = (q * p).toFixed(2);
}

function addItem() {
    if (!tsInstance) return;

    const id = tsInstance.getValue();
    const itemData = tsInstance.options[id];
    const qty = document.getElementById('f-qty').value;
    const price = document.getElementById('f-price').value;

    if (!id || qty <= 0) return alert("Select item and quantity");

    const row = document.createElement('tr');
    row.className = 'po-row';
    row.innerHTML = `
        <td>
            <input type="text" class="itemName" value="${itemData.name}" readonly>
            <input type="hidden" class="itemid" value="${id}">
        </td>
        <td><input type="number" class="qty" value="${qty}" oninput="reIndexAndTotal()"></td>
        <td><input type="number" class="price" value="${price}" oninput="reIndexAndTotal()"></td>
        <td class="subtotal">0.00</td>
        <td><button type="button" onclick="this.closest('tr').remove(); reIndexAndTotal();">Remove</button></td>
    `;

    document.getElementById('po-row-container').appendChild(row);

    // Reset footer
    tsInstance.clear();
    document.getElementById('f-qty').value = 1;
    document.getElementById('f-price').value = '';
    // updateFooterSubtotal();
    reIndexAndTotal();
}

function removeRow(btn) {
    btn.closest('tr').remove();
    reIndexAndTotal();
}

function reIndexAndTotal() {
    let grandTotal = 0;
    const rows = document.querySelectorAll('.po-row');

    rows.forEach((row, index) => {
        // Update names for Spring Binding: items[0].itemId, items[1].qty, etc.
        row.querySelector('.itemid').name = `items[${index}].itemId`;
        row.querySelector('.itemName').name = `items[${index}].name`;
        row.querySelector('.qty').name = `items[${index}].qty`;
        row.querySelector('.price').name = `items[${index}].price`;

        // Calculate Subtotal for the row
        const q = parseFloat(row.querySelector('.qty').value) || 0;
        const p = parseFloat(row.querySelector('.price').value) || 0;
        const sub = q * p;
        row.querySelector('.subtotal').innerText = sub.toFixed(2);

        grandTotal += sub;
    });

    document.getElementById('grand-total').innerText = grandTotal.toFixed(2);
}

function calculateSubTotal(input) {
    // Allows live updates if user edits a row already in the table
    reIndexAndTotal();
}

function resetFooter() {
    document.getElementById('f-id').value = '';
    document.getElementById('f-qty').value = '';
    document.getElementById('f-price').value = '';
}

function initTom() {
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
