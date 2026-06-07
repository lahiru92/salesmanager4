## Plan: Reuse PO form for create/edit/view

TL;DR - Keep a single `po/form.html` for create/edit/view but avoid scattered `th:if` by extracting field and action fragments, passing a single `mode` (or booleans) from controllers, and centralizing attribute logic (readonly/disabled/hidden) via fragment parameters or `th:attr`.

**Steps**
1. Add a `mode` model attribute in controller endpoints (values: `create`, `edit`, `view`) and/or booleans `editable`/`viewOnly` where helpful. (*depends on controllers*)
2. Extract each repeated input row into a fragment (e.g., `fragments/input-field.html`) with parameters: `name`, `label`, `value`, `readonly`, `disabled`, `required`, `type`. (*parallelizable*)
3. Replace inline inputs in `templates/po/form.html` with fragment calls, passing `readonly=${mode=='view'}` or a computed boolean. Use `th:replace` or `th:insert` and fragment parameters. (*depends on step 2*)
4. Extract the action bar (Save, Edit, Cancel, Print) into a fragment that renders buttons based on `mode` and `status` (e.g., show Edit only in `view` and Save only in `create|edit`).
5. Centralize validation/message blocks in a fragment so error rendering logic is consistent across modes.
6. Keep dynamic parts (item rows) as an item-row fragment already present; ensure it accepts a `readonly`/`disabled` param and that TomSelect is initialized only when not view-only.
7. Optionally, add a small helper fragment that returns attribute strings: `th:attr\"readonly=${readonly}? 'readonly' : null, disabled=${disabled}? 'disabled' : null\"` to avoid repeating attribute logic.
8. Update controller tests / manual checks: create/edit/view flows render correctly, fields toggle readonly, buttons appear/disappear, and validation behaves as expected.

**Relevant files**
- [src/main/resources/templates/po/form.html](src/main/resources/templates/po/form.html) — main template to refactor
- [src/main/resources/templates/po/item-row.html](src/main/resources/templates/po/item-row.html) — dynamic PO line fragment (reuse)
- [src/main/resources/templates/fragments/combobox.html](src/main/resources/templates/fragments/combobox.html) — existing fragment pattern to copy
- [src/main/resources/templates/fragments/pagination.html](src/main/resources/templates/fragments/pagination.html) — example of parametrized fragment
- [src/main/java/com/example/salesmanager4/purchase_order/PurchaseOrderController.java](src/main/java/com/example/salesmanager4/purchase_order/PurchaseOrderController.java) — update endpoints to supply `mode`

**Verification**
1. Smoke test in browser: open create, edit, view endpoints and confirm buttons/fields match expected mode.
2. Exercise create/edit submission and validation.
3. Automated test: add or update a controller unit/integration test that asserts the model contains `mode` and that the view contains expected fragments/buttons.

**Decisions & Rationale**
- Use a single `mode` string: simplest to pass and reason about in templates. Derive booleans in the template when needed.
- Prefer fragments with parameters over many `th:if` blocks: fragments centralize attribute logic and drastically reduce template branching.

**Further Considerations**
1. If you want to allow toggling from view→edit on the page, keep JS to enable inputs rather than re-rendering the whole form.
2. If access control differs by role, let the controller compute `editable` instead of trusting a URL parameter.
