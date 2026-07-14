export function init(root) {
  recalc();
  drawerRecalc();
}

export function addDeposit() {
  const container = document.querySelector('#deposit-rows');
  const template = document.querySelector('#deposit-row-template');
  if (!container || !template) return;

  const clone = template.content.firstElementChild.cloneNode(true);
  container.appendChild(clone);
  reindex();
}

export function removeDeposit(btn) {
  btn.closest('tr').remove();
  reindex();
  recalc();
}

function reindex() {
  document.querySelectorAll('.deposit-row').forEach((row, index) => {
    row.querySelector('.deposit-bank').setAttribute('name', `deposits[${index}].bank`);
    row.querySelector('.deposit-ref').setAttribute('name', `deposits[${index}].referenceNumber`);
    row.querySelector('.deposit-amount').setAttribute('name', `deposits[${index}].amount`);
  });
}

// Salesman handover: variance = physical cash + CDM deposits - expected
export function recalc() {
  const expectedEl = document.querySelector('#cashbal-expected');
  if (!expectedEl) return;

  const expected = parseFloat(expectedEl.dataset.value) || 0;
  const declared = parseFloat(document.querySelector('#declaredCash')?.value) || 0;

  let cdm = 0;
  document.querySelectorAll('.deposit-amount').forEach(input => {
    cdm += parseFloat(input.value) || 0;
  });

  const received = declared + cdm;
  setText('#cashbal-cdm-total', cdm);
  setText('#cashbal-received', received);
  setVariance('#cashbal-variance', received - expected);
}

// Drawer: expected closing = opening + handover cash + supplier refunds - supplier payments
export function drawerRecalc() {
  const calc = document.querySelector('#drawer-calc');
  if (!calc) return;

  const handoverCash = parseFloat(calc.dataset.handoverCash) || 0;
  const cashIn = parseFloat(calc.dataset.cashIn) || 0;
  const cashOut = parseFloat(calc.dataset.cashOut) || 0;
  const ledgerIn = parseFloat(calc.dataset.ledgerIn) || 0;
  const ledgerOut = parseFloat(calc.dataset.ledgerOut) || 0;
  const opening = parseFloat(document.querySelector('#openingBalance')?.value) || 0;

  const expected = opening + handoverCash + cashIn + ledgerIn - cashOut - ledgerOut;
  setText('#drawer-expected', expected);

  const counted = document.querySelector('#countedClosing')?.value;
  const varianceEl = document.querySelector('#drawer-variance');
  if (!varianceEl) return;

  if (counted === '' || counted == null) {
    varianceEl.innerText = '—';
    varianceEl.classList.remove('text-danger', 'text-success');
    return;
  }

  setVariance('#drawer-variance', parseFloat(counted) - expected);
}

function setText(selector, value) {
  const el = document.querySelector(selector);
  if (el) el.innerText = value.toFixed(2);
}

function setVariance(selector, variance) {
  const el = document.querySelector(selector);
  if (!el) return;
  el.innerText = variance.toFixed(2);
  el.classList.toggle('text-danger', variance < 0);
  el.classList.toggle('text-success', variance > 0);
}
