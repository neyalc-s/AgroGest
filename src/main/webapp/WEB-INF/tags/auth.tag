<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="titulo" required="true" %>
<%@ attribute name="tipo" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="pt-MZ">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="theme-color" content="#183D32">
  <title><c:out value="${titulo}"/> · AgroGest</title>
  <link rel="icon" href="${cp}/assets/favicon.svg" type="image/svg+xml">
  <link rel="stylesheet" href="${cp}/assets/style.css?v=botanical-4">
  <script defer src="${cp}/assets/login.js?v=botanical-1"></script>
</head>
<body class="auth-page ${tipo == 'login' ? 'auth-login' : 'auth-register'} ${not empty erro ? 'auth-has-error' : ''}">
  <c:if test="${tipo == 'login'}"><img class="auth-backdrop" src="${cp}/assets/img/login-fields.svg" alt="" width="560" height="300"></c:if>
  <div class="auth-visual">
    <a class="brand" href="${cp}/login"><t:marca/></a>
    <div class="auth-headline">
      <span class="eyebrow">DA TERRA AO FUTURO</span>
      <h1>O futuro cresce<br>com uma boa<br><em>gestão.</em></h1>
      <p>Da primeira semente à próxima colheita.<br>O seu negócio, em harmonia.</p>
    </div>
    <c:if test="${tipo != 'login'}"><img src="${cp}/assets/img/terraces.svg" alt="Linhas de cultivo e um sol em tons de dourado"></c:if>
    <div class="auth-location">NAMPULA, MOÇAMBIQUE <span>●</span> AGROGEST</div>
  </div>
  <main class="auth-main">
    <div class="auth-card">
      <a class="brand auth-mobile-brand" href="${cp}/login"><t:marca/></a>
      <span class="eyebrow green">BEM-VINDO AO AGROGEST</span>
      <h1><c:out value="${titulo}"/></h1>
      <c:if test="${not empty erro}"><div class="alert error" role="alert"><c:out value="${erro}"/></div></c:if>
      <jsp:doBody/>
    </div>
    <p class="auth-footer">Crescer começa com uma boa gestão.</p>
  </main>
</body>
</html>
