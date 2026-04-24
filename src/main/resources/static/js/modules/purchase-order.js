let itemSelectInstance;
let supplierSelectInstance;

export function init(root) {
  initItemSelect(root);
  initSupplierSelect(root);
}

function reIndexAndTotal() {
    let grandTotal = 0;
    const rows = document.querySelectorAll('.po-row');

    if (supplierSelectInstance) {
      if (rows.length === 0) {
        supplierSelectInstance.unlock();
      } else {
        supplierSelectInstance.lock();
      }
    }

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

function initItemSelect(root) {
  const el = root.querySelector('#f-id');
  if (!el) return;

  if (el.tomselect) {
    el.tomselect.destroy();
  }

  itemSelectInstance = new TomSelect(el, {
    valueField: 'itemId',
    labelField: 'name',
    searchField: ['name'],

    load: function(query, callback) {
      if (!query.length) return callback();

      fetch('/items/api/list?q=' + query)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    }
  });
}

function initSupplierSelect(root) {
  const el = root.querySelector('#supplier');
  if (!el) return;

  if (el.tomselect) {
    el.tomselect.destroy();
  }

  supplierSelectInstance = new TomSelect(el, {
    valueField: 'supplierId',
    labelField: 'name',
    searchField: ['name'],

    load: function(query, callback) {
      if (!query.length) return callback();

      fetch('/suppliers/api/list?q=' + query)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    }
  });
}


export function addItem() {

    // Validate if item and supplier selects are initialized
    if (!itemSelectInstance) return;
    if (!supplierSelectInstance) return;

    // Get selected item data
    const id = itemSelectInstance.getValue();
    const itemData = itemSelectInstance.options[id];
    const qty = document.getElementById('f-qty').value;
    const price = document.getElementById('f-price').value;

    // Supplier must be selected before adding items
    if (!supplierSelectInstance.getValue()) {
      alert("Please select a supplier before adding items.");
      return;
    }

    // Item and qty required
    if (!id || qty <= 0) return alert("Select item and quantity");

    // Check for duplicates
    const existingRows = document.querySelectorAll('.po-row');
    for (let row of existingRows) {
        if (row.querySelector('.itemid').value === id) {
            row.classList.add('table-warning');
            setTimeout(function() {
                row.classList.remove('table-warning');
            }, 2000);

            itemSelectInstance.wrapper.classList.add('is-invalid');
            return;
        }
    }

    // Add new row to the table
    const row = document.createElement('tr');
    row.className = 'po-row';
    row.innerHTML = `
        <td>
            <input type="text" class="itemName form-control-plaintext" value="${itemData.name}" readonly>
            <input type="hidden" class="itemid" value="${id}">
        </td>
        <td><input type="number" class="qty form-control-plaintext" value="${qty}" oninput="reIndexAndTotal()" readonly></td>
        <td><input type="number" class="price form-control-plaintext" value="${price}" oninput="reIndexAndTotal()" readonly></td>
        <td class="subtotal">0.00</td>
        <td><button class="btn btn-outline-danger" type="button" data-action="po:remove-item"><i class="bi-trash"></i></button></td>
    `;

    document.getElementById('po-row-container').appendChild(row);

    // Reset footer
    itemSelectInstance.clear();
    document.getElementById('f-qty').value = '';
    document.getElementById('f-price').value = '';
    // updateFooterSubtotal();
    reIndexAndTotal();
    itemSelectInstance.focus();
}

export function removeItem(btn) {
    btn.closest('tr').remove();
    reIndexAndTotal();
}

export function subtotal(e) {
  const row = e.target.closest('tr');
  if (!row) return;

  const qty = parseFloat(row.querySelector('#f-qty')?.value) || 0;
  const price = parseFloat(row.querySelector('#f-price')?.value) || 0;

  const subtotal = qty * price;

  row.querySelector('#f-subtotal').value = subtotal.toFixed(2);

}