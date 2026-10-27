<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:auth titulo="Entrar na sua conta">
  <p class="muted">Continue a cuidar do que faz crescer.</p>
  <form method="post" action="${cp}/login" class="stack" data-login-form>
    <t:csrf/>
    <label>Email
      <input type="email" name="email" value="<c:out value='${param.email}'/>" placeholder="nome@exemplo.mz" maxlength="160" autocomplete="username" required autofocus>
    </label>
    <label>Palavra-passe
      <input type="password" name="senha" placeholder="A sua palavra-passe" maxlength="128" autocomplete="current-password" required>
    </label>
    <button class="btn full" type="submit">Entrar <span>→</span></button>
  </form>
  <p class="auth-switch">Ainda não tem conta? <a href="${cp}/registo">Criar conta de cliente</a></p>
  <c:if test="${demoLogin}">
    <section class="demo-selector" aria-labelledby="demo-title">
      <div class="demo-selector-heading">
        <h2 id="demo-title">Utilizadores disponíveis</h2>
        <span class="badge orange">Temporário</span>
      </div>
      <p class="demo-status" data-demo-status role="status">Clique numa conta para preencher os campos. Depois, carregue em Entrar.</p>
      <div class="demo-account-grid">
        <c:forEach items="${acessosDemo}" var="a">
          <button type="button" class="demo-account" data-demo-account
              data-demo-email="<c:out value='${a.email}'/>"
              data-demo-senha="<c:out value='${a.credencial}'/>"
              aria-pressed="false" ${a.ativo ? '' : 'disabled'}>
            <span class="demo-role">${a.perfil == 'ADMIN' ? 'Administrador' : 'Cliente'}${a.ativo ? '' : ' · Inactivo'}</span>
            <strong><c:out value="${a.nome}"/></strong>
            <small><c:out value="${a.email}"/></small>
          </button>
        </c:forEach>
      </div>
      <noscript><p>Active JavaScript para preencher por clique, ou introduza as suas credenciais nos campos acima.</p></noscript>
    </section>
  </c:if>
</t:auth>
