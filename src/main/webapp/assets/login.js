'use strict';

document.addEventListener('click', function (event) {
  const account = event.target.closest('[data-demo-account]');
  const form = document.querySelector('[data-login-form]');
  if (!account || account.disabled || !form) return;

  form.elements.email.value = account.dataset.demoEmail;
  form.elements.senha.value = account.dataset.demoSenha;
  for (const input of [form.elements.email, form.elements.senha]) {
    input.dispatchEvent(new Event('input', { bubbles: true }));
    input.dispatchEvent(new Event('change', { bubbles: true }));
  }
  document.querySelectorAll('[data-demo-account]').forEach(function (button) {
    button.setAttribute('aria-pressed', String(button === account));
  });
  const name = account.querySelector('strong').textContent;
  document.querySelector('[data-demo-status]').textContent = name + ': campos preenchidos. Carregue em Entrar.';
  form.querySelector('button[type="submit"]').focus();
});
