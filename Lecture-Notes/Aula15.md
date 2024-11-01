# Aula 15 - Autenticadores em Contexto Web

Nas aulas anteriores, discutimos uma série de tópicos: autenticação baseada em *password*, o protocolo HTTP e, finalmente, os *Cookies*. Nesta aula, juntaremos os conhecimentos adquiridos nas aulas anteriores para estudar como aplicações web efetivamente realizam o processo de autenticação. Mais especificamente, estudaremos algumas possíveis implementações de **autenticadores**.

## Autenticação "basic" do HTTP

A partir da sua versão 1.0, o HTTP passou a incluir suporte um mecanismo rudimentar de autenticação denominado ***Basic Authentication***. A ideia era simples: o cliente, ao realizar uma requisição, incluía as credenciais do utilizador num *header field* específico denominado `Authorization`. Mais especificamente, o formato deste *header* é `Authorization: Basic <credenciais>`, onde `<credenciais>` corresponde à *string* `<ID>:<Password>` (*i.e.,*, a concatenação do ID e da *password* do utilizador com um carácter dois-pontos) **codificada em Base64**.

Por exemplo, suponha que o nome do utilizador seja *alice* e a *password* seja *123456*. O seguinte *header field* seria adicionado à requisição:

```http
Authorization: Basic YWxpY2U6MTIzNDU2
```

Na mesma altura, foi definido um segundo mecanismo de autenticação similar denominado ***Digest Access Authentication***. Este mecanismo utiliza o mesmo *header* `Authorization`, mas ao invés de enviar as credenciais em texto plano codificado em Base64, no modo *Digest* o cliente enviava um *hash* da concatenação do nome de utilizador e *password*. A vantagem deste modo em relação ao *Basic* é que, caso a requisição seja intercetada por um atacante, ele não será capaz de recuperar a *password*, devido à não inversibilidade das funções de *hash*.

Seja qual for o modo, ao receber tal requisição, o servidor extrai as credenciais do *header*, faz a verificação e, caso bem-sucedida, envia a resposta solicitada. Caso contrário, um erro `401 Unauthorized` é enviado.

Mas como o *browser* sabe em qual ou quais requisições deve enviar as credenciais? A resposta é que ele não sabe *a priori* e, portanto, a princípio realiza todas as requisições **sem a informação das credenciais**. Eventualmente, o *browser* pode requisitar um recurso específico que é protegido no servidor e receber como resposta um erro `401 Unauthorized`. Quando isto ocorre, o *browser* solicita as credenciais ao utilizador e envia uma nova requisição atualizada. 

### Um Exemplo de Servidor com Autenticação *Basic*

Vejamos agora como podemos criar um servidor *web* (ou, mais concretamente, uma aplicação *web*) que utilize o *basic authentication* do HTTP para proteger um recurso específico. 

Para este e os demais exemplos mostrados nesta aula (e nas demais aulas desta UC), construiremos o servidor utilizando a linguagem `node.js`. Mais especificamente, utilizaremos o módulo `express` para simplificar o desenvolvimento. Em todos os exemplos, construiremos uma aplicação sobre HTTPS, já que qualquer solução de autenticação estudada aqui será insegura sobre HTTP.

Além disto, os exemplos incluídos nesta aula serão apresentados de maneira incremental: começaremos por um código básico de uma aplicação *web* genérica sem autenticação e, passo a passo, fazermos modificações pontuais para implementar abordagens distintas de autenticação.

A versão básica, **sem autenticação**, da aplicação pode ser vista a seguir:

```node
// Built-in HTTPS support
const https = require("https");
// Handling GET request (npm install express)
const express = require("express");
// Load of files from the local file system
var fs = require('fs');

const PORT = 4433;
const app = express();

// Get request for resource /
app.get("/*", function (req, res) {
    console.log(
        req.socket.remoteAddress
        + ' ' + req.method
        + ' ' + req.url);

    console.log(req.headers);

    res.send("<html><body><h1>Secure Hello World with node.js</h1>You are at " + req.url + "</body></html>");
});


// configure TLS handshake
const options = {
    key: fs.readFileSync('secure-server-privkey.pem'),
    cert: fs.readFileSync('secure-server.pem')
};

// Create HTTPS server
https.createServer(options, app).listen(PORT,
    function (req, res) {
        console.log("Server started at port " + PORT);
    }
);
```

O código começar por carregar os módulos necessários: `express` e `https` (este último porque queremos que a aplicação corra sobre HTTPS). Também carregamos o módulo `fs` porque em certo ponto teremos que interagir com ficheiros armazenados no servidor.

De seguida, construímos instanciamos uma aplicação `express` e definimos uma *callback* responsável por responder por requisições do tipo `GET` a qualquer endereço solicitado pelo cliente. Nesta versão básica, a função simplesmente imprime no *log* algumas informações básicas sobre a requisição. Como não há ainda autenticação, a função sempre retorna uma resposta positiva apresentando uma mensagem qualquer ao utilizador (*"Secure Hello World with node..."*)

Depois da definição desta *callback*, declara-se um objeto chamado `options`. Este objeto é mais tarde passado como parâmetro para o método `https.createServer()` e tem por objetivo definir algumas informações básicas acerca das chaves e certificados utilizados pelo servidor. Particularmente, o código preenche duas propriedades do objeto: a `key`, que contém a chave privada a ser usada pelo servidor (lida a partir dum ficheiro) e a `cert`, que contém uma cadeia de certificados cujo certificado folha é o certificado do servidor (também lida dum ficheiro).

Se corrermos esta versão da aplicação e acedermos ao servidor utilizando um *browser*, devemos ter acesso a qualquer URL daquele servidor, sempre recebendo a mensagem positiva a cada nova requisição. Em momento algum utiliza-se qualquer tipo de autenticação.

O que necessitamos modificar neste código base para usar o *basic authentication* do HTTP? Resumidamente, precisamos que a *callback* que trata das requisições solicite ao *browser* que adicione o *header field* `Authorization` com as credenciais do utilizador, **caso o mesmo já não esteja presente**. Isto pode ser feito alterando-se o código da *callback* para algo como:

```node
app.get("/*", function (req, res) {
    console.log(
        req.socket.remoteAddress
        + ' ' + req.method
        + ' ' + req.url);


    console.log(req.headers);
    const authheader = req.headers.authorization;

    if (!authheader) {

        res.setHeader('WWW-Authenticate', 'Basic realm=www.secure-server.edu');
        res.statusCode = 401;
        res.send("<html><body>Please, provide authentication credentials.</body></html>");

        return;
    }

    res.send("<html><body><h1>Secure Hello World with node.js</h1>You are at " + req.url + "</body></html>");
});
```

Repare como declaramos a constante `authheader`, inicializada com o valor que consta na propriedade `authorization` do objeto `req.headers`. Na linha seguinte, testa-se o valor desta constante de forma a determinar se o *header field* estava, realmente, presente na requisição recebida. Se não, é gerada uma resposta com *status* 401, indicando falta de autorização do cliente para aceder àquele conteúdo solicitado. 

Adicionalmente, note que é utilizado o método `setHeader()` do objeto de resposta para adicionar a esta resposta um *header field* denominado `WWW-Authenticate`. Este *header field* define qual ou quais métodos de autenticação são permitidos pelo servidor para o recurso solicitado. Neste exemplo, especificamos o valor `Basic realm=www.secure-server.edu`. Aqui, `Basic` indica que o método de autenticação suportado é o *basic authentication* do HTTP. Já o parâmetro `realm` contém uma *string* que informa ao cliente qual "porção" do servidor é coberta por esta autenticação. Embora o parâmetro `realm` seja mandatório, ele não tem grande efeito prático na autenticação em si. Alguns *browsers* exibem o valor do *realm* para o utilizador (como forma de supostamente o utilizador saber para que propósito serão utilizadas as suas credenciais), mas isto nem sempre ocorre.

Ao receber esta resposta de *status* 401, o *browser* deverá exibir uma janela solicitando que o utilizador informe suas credenciais para acesso àquela página. Em seguida, o *browser* enviará uma nova requisição contendo o *header field* `Authorization`.

Note, portanto, que a função também deve estar pronta para a hipótese de receber uma requisição já contendo as credenciais. Neste caso, a resposta gerada deverá conter o conteúdo solicitado, o que é feito na última linha de código.

Um problema desta versão da aplicação é que, embora ela solicite as credenciais ao cliente/utilizador, o código não verifica se a *password* é válida (ou mesmo se o nome de utilizador está registado no sistema). Isto, no entanto, é relativamente simples. Basta acedermos o valor do *header* `Authorization` recebido na requisição, fazermos a descodificação (lembre que este valor é codificado em `Base64`), isolarmos as componentes *username* e *password* (lembre-se que estas são separadas por um dois-pontos) e fazermos as verificações. O trecho de código a seguir mostra como isto pode ser (feito de forma muito simplificada):

```node
    var usernamePlusPassword = Buffer.from(authheader.split(" ")[1], 'base64').toString();
    var credentials = usernamePlusPassword.split(":");
    var username = credentials[0];
    var password = credentials[1];

    if (username != "admin" || password != "123456") {

        res.setHeader('WWW-Authenticate', 'Basic realm=www.secure-server.edu');
        res.statusCode = 401;
        res.send("<html><body>Invalid credentials. Try again.</body></html>");

        return;
    }
```

Note que neste trecho, por simplicidade, apenas comparamos `username` e `password` a literais declarados no código. Em um sistema real, o código deveria gerar as informações de validação a partir destes valores e, depois, realizar a validação em si junto à base de utilizadores registados.

De toda forma, se a verificação das credenciais falha, o código simplesmente gera outra resposta com *status* 401, incluindo, novamente, a especificação do tipo de autenticação suportada.

### Autenticação *Basic* e HTTP *Stateless*

Embora este método de autenticação funcione, ele tem vários inconvenientes. O principal tem relação à natureza *stateless* do HTTP: como o protocolo não guarda estado, o cliente é obrigado a **incluir as credenciais em todas as requisições realizadas a recursos protegidos no servidor**. Dito de outra forma: o servidor não se lembra de que um cliente realizou uma autenticação bem-sucedida numa requisição passada quando processa novas requisições.

Isto significa que o *browser* é obrigado a guardar a *password* do utilizador localmente para a hipótese desta ser necessária em requisições posteriores. Isto aumenta a responsabilidade do *browser* em termos de segurança. Alternativamente, o *browser* pode esquecer a *password*, mas, neste caso, terá que solicitá-la ao utilizador toda vez que realizar um acesso a um recurso protegido.

Do lado do servidor, a inclusão das credenciais em cada requisição significa que o servidor será obrigado a fazer a validação da *password* múltiplas vezes, aumentando o tempo de processamento das requisições.

## Autenticadores

Uma alternativa para evitar os inconvenientes dos modos de autenticação *Basic* e *Digest* do HTTP é o uso de **autenticadores**. Neste contexto, um autenticador refere-se a um pequeno documento digital gerado pelo servidor e repassado ao cliente consonante uma autenticação bem-sucedida. Após obter o autenticador, o *user-agent* do utilizador (*i.e.*, o *browser*) apresenta-o automaticamente a cada nova requisição.

Em resumo, o uso de autenticadores pode ser dividido em duas fases:

- Fase 1: o utilizador apresenta as suas credenciais (*e.g.*, *username* + *password*) ao servidor. O servidor valida as credenciais e, em caso de sucesso, gera um autenticador, enviado ao cliente na mensagem de resposta.
- Fase 2: a cada nova requisição, o *browser* inclui o autenticador recebido. Por sua vez, ao receber uma requisição com um autenticador, o servidor verifica a validade do mesmo. Caso seja válido, o servidor dá acesso ao recurso requisitado.

Note que, na fase 2, o servidor precisa confirmar a **validade** do autenticador. Ou seja, é preciso ter certeza de que o autenticador apresentado não foi forjado. Como veremos mais à frente, isto pode implicar o uso de algum esquema de autenticidade, como um MAC ou uma Assinatura Digital.

## Ataques Relacionados a Autenticadores

Da perspectiva de um atacante, há três possíveis atividades maliciosas relativas a autenticadores.

A primeira é uma tentativa de **falsificação existencial**. Ou seja, um atacante pode objetivar obter um autenticador válido qualquer, independentemente de qual seja o utilizador associado a ele. 

Um objetivo mais ambicioso é a **falsificação seletiva**. Neste caso, o atacante tem um utilizador específico como alvo e, portanto, deseja determinar um autenticador válido para aquele utilizador.

Por fim, um atacante pode objetivar obter a chave utilizada na criação dos autenticadores por parte do servidor. Lembre-se que servidores precisam verificar a validade dos autenticadores que recebem. Em certas situações, isto pode envolver o emprego de um MAC ou de uma Assinatura Digital. Se o atacante consegue obter a chave usada pelo servidor para este esquema criptográfico, então ele passa a ser capaz de forjar autenticadores para quaisquer utilizadores do sistema sem que o servidor seja capaz de detectar.

## Autenticadores com *Cookies*

Vamos agora discutir mais detalhadamente como um autenticador pode ser implementado utilizando-se *cookies*.

Tipicamente, o sistema possui uma página de *login* com um formulário que permita ao utilizador enviar suas credenciais (*username* + *password*). Enquanto o utilizador não possui um autenticador válido, qualquer tentativa de acesso a um recurso protegido do sistema resulta em um redirecionamento para esta página de *login* - ou, ao menos, na informação de que o utilizador precisa realizar *login* para aceder ao recurso.

Quando o utilizador submete suas credenciais através desta página, o servidor procede à validação da *password* de acordo com as informações de validação encontradas na sua base local. Se a validação é bem-sucedida, o sistema cria um novo autenticador e o envia ao utilizador. Note que é necessário constar do autenticador a informação de qual é a identidade do cliente autenticado, de forma que o sistema possa fazer quaisquer personalizações necessárias ao conteúdo ou aplicar políticas de controlo de acesso adequadas.

Uma das formas de realizar o envio do autenticador ao cliente é justamente através de *cookies*. Ou seja, se a validação das credenciais do cliente é bem-sucedida, o sistema inclui na resposta HTTP um *cookie* (através de um *header field* `Set-Cookie`) cujo valor corresponde ao valor do autenticador.

Neste caso, todas as requisições subsequentes feitas pelo cliente ao sistema carregarão consigo o *cookie* relativo ao autenticador. Com isto, o servidor poderá extrair o valor do autenticador, validá-lo e, apenas caso seja válido, entregar o recurso solicitado pelo cliente.

### Autenticadores com *Cookies*: Implementação

O funcionamento geral dos autenticadores com *cookies* descrito na seção anterior pode ser implementado de algumas formas diferentes. Para ilustrar isto, começaremos por uma versão **insegura** que, posteriormente, será corrigida.

Em relação à versão anterior do sistema de exemplo, nestas próximas versões teremos *callbacks* diferentes para tratar das requisições a recursos diferentes do sistema. Particularmente, a partir de agora, assumiremos que o sistema é organizado no seguinte conjunto de recursos:

- `/privado`: recurso privado que só pode ser acedido por utilizadores previamente autenticados.
- `/login`: recurso que representa a interface de *login* do sistema. Requisições do tipo `GET` resultam na exibição de um pequeno formulário para que o utilizador proveja seu *username* e sua *password*. A submissão deste formulário é feita através de uma requisição do tipo `POST` ao mesmo recurso.

A *callback* que trata das requisições `GET` ao *end-point* `/login` pode ser implementada de forma similar ao código a seguir:

```node
app.get("/login", function(req, res) {

    var form = '<form action="/login" method="POST">';
    form += '<label for="username">Username:</label><br>';
    form += '<input type="text" id="username" name="username" value=""><br>';
    form += '<label for="pass">Password:</label><br>';
    form += '<input type="password" id="pass" name="pass" value=""><br><br>';
    form += '<input type="submit" value="Submit">';
    form += '</form>';
    res.send("<html><body><h1>Login</h1>" + form + "</body></html>");
});
```

O trecho de código simplesmente gera como resposta uma pequena página HTML contendo o formulário para o *login*.

O código da *callback* que faz o tratamento das requisições do tipo `POST` é um pouco mais sofisticado:

```node
app.post("/login", function(req, res) {

    if (req.body.username != "admin" || req.body.pass != "123456") {

        res.send("<html><body>Invalid credentials. Try again.</body></html>");

        return;
    }

    res.cookie("auth", "valid");
    res.send("<html><body><h1>Login Successful</h1></body></html>");
});
```

A primeira parte da função simplesmente extrai da requisição os valores dos campos `username` e `password` do formulário e realiza a validação das credenciais. Note que aqui, mais uma vez, esta validação é rudimentar, feita através de comparações com literais, mas que o trecho correspondente poderia ser substituído por uma validação contra uma base de utilizadores.

No caso de a validação ser bem-sucedida, é gerado um *cookie* denominado `auth` contendo um autenticador bastante simplificado. Neste caso, o autenticador tem apenas a informação `valid`, a indicar que a autenticação foi bem-sucedida. O corpo da resposta é apenas uma mensagem de sucesso no *login* a ser exibida para o utilizador.

Data esta estrutura, a *callback* responsável pelo *end-point* `privado` teria o seguinte código:

```node
app.get("/privado", function (req, res) {
    if (req.cookies.auth == "valid") {

        res.send("<html><body><h1>Private Area!</h1>Access granted.</body></html>");
    }
    else {

        res.send("<html><body><h1>Private Area!</h1>Access blocked. Please, log in.</body></html>");
    }
});
```

Note que, basicamente, a *callback* verifica se o *cookie* `auth` existe e tem valor `valid`. Se sim, assume-se que o utilizador efetuou uma autenticação bem-sucedida e dá-se acesso ao recurso privado. Caso contrário, sugere-se ao utilizador que realize o *login*.

O código acima faz o que se deseja para utilizadores legítimos, mas é completamente inseguro em presença de um atacante. Isto porque as únicas coisas que o sistema verifica quando ocorrem requisições ao recurso privado são se o *cookie* `auth` existe e se tem um valor específico constante. Um atacante que observe o funcionamento do sistema facilmente conseguiria forjar o *cookie* com o autenticador, já que não há qualquer verificação de autenticidade do conteúdo do mesmo.

Desta forma, para implementarmos estes autenticadores baseados em *cookies* de forma segura, precisamos fazer alterações no código de maneira a impedir que um autenticador válido seja forjável por um atacante. De maneira geral, há duas abordagens para isto: a do identificador de sessão e a baseada em MAC.

#### Autenticadores com *Cookies*: Identificador de Sessão

Nesta primeira abordagem, quando o sistema cria um novo autenticador como consequência de uma autenticação bem-sucedida, as informações relevantes (*e.g.*, a identidade do utilizador) são armazenadas em uma base de dados / estrutura de dados no próprio servidor. Neste caso, o valor repassado ao cliente no *cookie* é apenas um identificador de sessão que serve como uma chave primária nesta base de dados (ou um índice numa estrutura de dados).

Em relação à versão anterior insegura, esta nova versão tem alterações nas *callbacks* referentes às requisições `POST` ao *end-point* `/login` e ao *end-point* `/privado`. Uma possível implementação da primeira seria:

```node
app.post("/login", function(req, res) {

    if (req.body.username != "admin" || req.body.pass != "123456") {

        res.send("<html><body>Invalid credentials. Try again.</body></html>");

        return;
    }

    authCode = randomBytes(32).toString('hex');
    authUsers[authCode] = {name: req.body.username};
    res.cookie("auth", authCode);
    res.send("<html><body><h1>Login Successful</h1></body></html>");
});
```

Note como a função cria um identificador de sessão aleatório (variável `authCode`) e o utiliza para registar a sessão autenticada do utilizador (aqui, usamos um objeto `authUsers` para isto, mas tal informação poderia ser armazenada, por exemplo, numa base de dados relacional). Note também que podemos armazenar uma série de informações pertinentes ao utilizador nesta base, embora aqui, por simplicidade, tenhamos armazenado apenas o seu *username*. O que é retornado para o utilizador como autenticador é o próprio identificador de sessão.

Por outro lado, a *callback* do *end-point* privado poderia ser implementada da seguinte maneira:

```node
app.get("/privado", function (req, res) {
    if (req.cookies.auth in authUsers) {
        
        res.send("<html><body><h1>Private Area!</h1>Access granted. Logged in as " + authUsers[req.cookies.auth].name + "</body></html>");
    }
    else {

        res.send("<html><body><h1>Private Area!</h1>Access blocked. Please, log in.</body></html>");
    }
});
```

Aqui, simplesmente extraímos o identificador de sessão do autenticador recebido via *cookie* e verificamos se ele identifica uma sessão existente na base de dados local. Se sim, assume-se que o utilizador foi autenticado previamente.

Um ponto importante desta abordagem é que o valor do autenticador (*i.e.*, o identificador da sessão) deve ser **imprevisível** para um atacante. Repare como na *callback* responsável pelo *login*, utiliza-se a geração de um identificador aleatório de grande dimensão. Isto é essencial porque, caso o identificador seja previsível (por exemplo, um número de sequência incrementado a cada novo *login* bem-sucedido), um atacante pode facilmente adivinhar valores de autenticadores válidos, conseguindo assim uma falsificação existencial.

#### Autenticadores com *Cookies*: *Message Authentication Code*

Em certas situações, podemos não querer que o servidor seja responsável por armazenar a informação da sessão. Ao contrário, pode ser mais adequado, a depender do caso, que o próprio autenticador contenha a informação de sessão (*e.g.,* o nome do utilizador e outras informações pessoais), de forma que o sistema possa consultar estes dados diretamente a partir do valor armazenado no *cookie*.

Neste caso, o valor do *cookie* relativo ao autenticador é não apenas um número aleatório mas, na verdade, uma estrutura de dados composta de campos de informação segundo alguma codificação. Mais a frente nesta aula daremos um exemplo mais concreto de uma possível codificação.

Assim como na alternativa anterior, um sistema que opta por esta abordagem precisa garantir que um atacante não seja capaz de forjar um autenticador. Logo, o servidor deve ter a capacidade de **verificar a autenticidade do autenticador** recebido no *cookie*. Felizmente, já estudamos esquemas criptográficos capazes de prover autenticidade de mensagens na primeira parte desta UC. 

Particularmente, uma hipótese é que o sistema utilize um MAC. Mais especificamente, o servidor pode ter uma chave simétrica secreta com o propósito específico de gerar/verificar marcas de autenticidade MAC. Quando ocorre uma autenticação bem-sucedida de um utilizador, o servidor cria o autenticador com as informações pertinentes e calcula um MAC sobre o mesmo. Então, ambos, autenticador e marca de autenticidade, são colocados no valor do *cookie* incluído na resposta do servidor. Por outro lado, ao receber um autenticador num *cookie* de uma requisição posterior, o servidor realiza a validação do MAC para verificar a validade do mesmo.

Note, adicionalmente, que em geral estamos preocupados aqui apenas com a autenticidade do autenticador. Por outro lado, se for desejável, é possível também utilizar uma cifra para garantir a confidencialidade das informações que constam do autenticador.

#### Autenticadores com *Cookies*: Outros Aspetos

Alguns outros aspetos devem ser levados em conta quando utilizamos autenticadores baseados em *cookies*, nomeadamente a sua validade temporal, a capacidade do sistema de realizar a revogação do autenticador e a configuração adequada das propriedades de segurança do *cookie*.

Em geral, quando um sistema gera um autenticador, é-lhe atribuída uma validade temporal. Ou seja, **desejamos que o autenticador expire** em algum momento no futuro, de forma a obrigar o utilizador a realizar um novo processo de autenticação. Isto é importante porque limita a utilidade de um autenticador para um atacante, caso este consiga obter algum válido.

Como os *cookies* possuem uma data de expiração dentre as suas propriedades, poderíamos ficar inclinados a utilizá-la como mecanismo de expiração: basicamente, ao gerarmos um autenticador, criaríamos o *cookie* correspondente com a data de expiração desejada e confiaríamos que o *browser* simplesmente pararia de enviá-lo após este momento. No entanto, esta solução não é segura, particularmente porque não devemos confiar que um atacante respeitará a data de expiração configurada para os *cookies*. Dito de outra forma: nada impede que o atacante continue a enviar o *cookie* contendo o autenticador mesmo após a sua (do *cookie*) expiração.

Desta forma, o controlo temporal da validade do autenticador tem de ser implementado através de mecanismos próprios. Caso o autenticador seja implementado através de um identificador de sessão apenas, a informação da validade temporal deve constar da informação de sessão armazenada do lado servidor de forma que esta não possa ser manipulada / alterada por um eventual atacante. Por outro lado, quando a informação de sessão é armazenada no próprio *cookie* (*i.e.*, como parte dos dados do autenticador), então a validade também deve estar lá presente e protegida por uma marca de autenticação como um MAC, de forma que não possa ser alterada por outras entidades que não sejam o próprio servidor. Seja como for, é preciso que esta informação seja **verificável pelo servidor a cada nova requisição recebida**.

Outra funcionalidade importante é a capacidade do servidor em **revogar uma autenticação** previamente realizada (antes da sua data de expiração). Um exemplo comum de uso desta revogação é quando o próprio utilizador solicita uma operação de *logout*. 

Para dar suporte a isto, há algumas alternativas, a depender de como o autenticador é implementado. Se o autenticador é um simples identificador de sessão e as informações de sessão são armazenadas pelo próprio servidor, então o servidor pode simplesmente invalidar a sessão. Isto é, o servidor pode ativar uma *flag* de *inválida* ou *não autenticada* nas informações da sessão, ou mesmo pode optar por remover a sessão da sua base de dados de sessões ativas. 

Por outro lado, esta estratégia não funciona caso as informações de sessão sejam contidas no próprio autenticador armazenado no *cookie*. Isto porque, ainda que o servidor solicite ao cliente que altere o valor do *cookie* para um que informe que a sessão foi invalidada, o cliente sempre pode ignorar este pedido e continuar a enviar o autenticador anterior (que diz que a sessão ainda é válida) nas requisições subsequentes.

Assim, nestes casos, uma alternativa melhor é que o servidor mantenha uma **lista de revogação de autenticadores**. Trata-se de uma lista armazenada no próprio servidor que contém uma relação de valores de autenticadores que, apesar de possuirem uma marca de autenticidade válida e ainda não estarem expirados, devem ser considerados revogados. Desta forma, ao receber uma requisição contendo um *cookie* com um autenticador, além de verificar sua autenticidade e validade, o servidor adicionalmente verifica se o autenticador não se encontra na lista de revogação. O autenticador, portanto, só é aceito pelo servidor se passar em todos estes testes.

Por fim, é importante discutirmos brevemente as configurações de segurança do *cookie* utilizado para armazenar o autenticador. Dado que um autenticador sinaliza a um servidor uma comunicação previamente autenticada, é fundamental garantir que atacantes não tenham acesso a eles. Por isto, autenticadores não podem ser transmitidos em texto plano. Logo, deve-se garantir que o *cookie* que transporta o autenticador só seja transmitido através de ligações cifradas, o que implica o uso da *flag* ***Secure***. Como vimos na aula passada, esta *flag* indica ao *browser* que só deve incluir o *cookie* em requisições realizadas por HTTPS (e, portanto, encapsuladas em TLS).

Também é importante garantir que eventuais códigos *Javascript* maliciosos não tenham acesso ao *cookie* do autenticador. Conforme discutido na última aula, o código *Javascript* de uma página tem acesso aos *cookies* daquela página através do objeto `document.cookies`. Se um atacante consegue, de alguma forma, adicionar um *script* malicioso à página, este *script* pode aceder ao objeto, obter os valores dos *cookies* e, por exemplo, enviá-los a um servidor controlado pelo atacante. Para evitar que isto possa ocorrer, o *cookie* relativo ao autenticador deve ser marcado com a propriedade `httpOnly = true`, o que significa que aquele *cookie* será incluído apenas nas requisições HTTP, e não no objeto `document.cookies`.

#### *Cross-Site Request Forgery*

O *Cross-Site Request Forgery*, ou simplesmente CSRF, é um ataque clássico que tenta explorar vulnerabilidades em autenticadores baseados em *cookies*. O ataque envolve três entidades:

1. Uma aplicação vulnerável, daqui em diante denominada apenas como **Aplicação B**.
2. Um **cliente previamente autenticado** na Aplicação B e, portanto, de posse de um autenticador válido. 
3. Uma aplicação maliciosa criada pelo atacante, daqui em diante denominada **Aplicação A**.

A Aplicação A, criada pelo atacante, pode ser qualquer página *web* simples. No entanto, o atacante incluirá nesta página alguma referência à URL de um recurso da Aplicação B. Por exemplo, digamos que a Aplicação B seja uma aplicação bancária e exista um recurso chamado `/transfer` que, quando acessado, realize a transferência de um valor especificado da conta do utilizador para outra conta. Por exemplo, uma implementação (bastante rudimentar) baseada no código do autenticador com *cookies* mostrado anteriormente poderia ser algo como:

```node
app.get("/transfer", function (req, res) {

    if (req.cookies.auth in authUsers) {

        currentBalance -= parseFloat(req.query.value);
        res.send("<html><body><h1>Private Area!</h1>User "
            + req.cookies.user
            + " just transfered EUR "
            + req.query.value
            + " to " + req.query.to + "</body></html>");
    }
    else {

        res.send("<html><body><h1>Private Area!</h1>Access blocked. Please, log in.</body></html>");
    }
});
```
Note que, nesta implementação, o valor a ser transferido e o destinatário da transferência são, ambos, especificados através de parâmetros da URL (`value` e `to`, respetivamente). 

Neste caso, o atacante pode incluir na página da sua Aplicação A um elemento como uma imagem ou um *iframe* que apontem para a URL `/transfer` na Aplicação B contendo parâmetros que solicitem a transferência de um certo valor para a conta dele próprio. Por exemplo:

```html
<html>
    <body>
        <h1>Welcome to some page!</h1>
        This page has some interesting content...
        <img src='https://www.secure-server.edu:4433/transfer?value=200&to=Trudy'/>
    </body>
</html>
```
Em geral, o atacante manipula as propriedades de estilo deste elemento para que ele não seja visível ao utilizador, embora neste exemplo simplificado não tenhamos feito isto. 

Considere agora que o cliente acede a esta página da Aplicação
ao A. Devido ao elemento que faz referência à URL da Aplicação A, o *browser* irá realizar a requisição HTTP correspondente. Lembre-se que, por hipótese, este cliente está autenticado junto à Aplicação B e, portanto, contém um *cookie* com um autenticador válido para aquela aplicação. Logo, este *cookie* - e, portanto, o autenticador - será enviado nesta requisição. 

Da perspetiva do servidor da Aplicação B, a requisição ao recurso `/transfer` será recebida com um autenticador válido. Assim, o servidor deverá aceitar a requisição e executar a ação correspondente (*i.e.*, realizar a transferência).

Em resumo: o atacante conseguiu, a partir da Aplicação A, **forçar o *browser* cliente a realizar uma ação não pretendida** na Aplicação B. Embora o cliente estivesse de facto autenticado junto à Aplicação B, note que a requisição, em si, era não autêntica, porque foi originada a partir de uma terceira parte não autorizada.

Note que, no cenário proposto, a requisição maliciosa é originada de uma página que **não pertence** ao site da Aplicação B. Este tipo de requisição, originada numa página de um site, mas direcionada a um recurso de outro, é denominada uma *Cross-Site Request*. O uso de *Cross-Site Requests* é relativamente comum na Internet: por exemplo, uma página pode incluir imagens que estão hospedadas em outro site. No entanto, como vimos aqui, tais requisições podem ser utilizadas para gerar ataques que exploram o uso de autenticadores armazenados em *cookies*.

Para evitar este tipo de problema, *cookies* que armazenam autenticadores devem ser configurados com a propriedade `SameSite=Strict`. Com esta configuração, um *browser* apenas inclui o *cookie* em requisições que foram originadas a partir de páginas no mesmo site pelo qual este foi definido. No exemplo de ataque descrito acima, como a requisição à Aplicação B é feita devido a uma referência encontrada numa página da Aplicação A, um *cookie* configurado com `SameSite=Strict` seria omitido da requisição, evitando o ataque.

## Autenticação com *Tokens*

A autenticação com *cookies*, quando implementada corretamente, provê uma solução adequada a muitos cenários de autenticação. Em particular, a autenticação com *cookies* adequa-se a aplicações em que o processo de autenticação está integrado no restante da aplicação. Aqui, por *integrado*, entenda-se que o recurso relacionado ao processo de *login* (*e.g.*, `/login`) corre no mesmo domínio que os demais recursos da aplicação. Isto porque o *cookie* que contém o autenticador, definido pelo servidor quando do acesso ao recurso de *login*, precisa ser incluído nas requisições feitas pelo cliente aos demais recursos da aplicação, mas *cookies* são associados com domínios das URLs.

Considere, por outro lado, uma aplicação que usa um serviço de autenticação separado. Este é um cenário bastante comum em aplicações *web* modernas, que frequentemente terceirizam a autenticação para grandes provedores de conteúdo, como a Google, a Apple e a Microsoft. Por exemplo, a plataforma *overleaf* (https://www.overleaf.com/), uma aplicação *web* para a edição colaborativa de documentos em LaTeX, permite que o utilizador autentique-se usando sua conta na Google. Neste caso, o utilizador é redirecionado para uma página de *login* da Google, na qual fornece suas credenciais. Caso a autenticação (junto à Google) seja bem-sucedida, de alguma forma, o servidor da aplicação original (*overleaf*) é informado e dá acesso ao utilizador aos recursos privilegiados.

Mas como a aplicação original sabe que o processo de autenticação foi bem-sucedido? Note que o servidor da Google não pode simplesmente definir um *cookie* com um autenticador, porque tal *cookie* ficaria restrito a domínios da própria Google e, portanto, não seria enviado a requisições realizadas à aplicação original. Portanto, é necessário que este autenticador seja transferido entre as partes de alguma outra maneira.

Esta outra maneira é o que se convencionou denominar um *token*. Resumidamente, um *token* é um documento digital emitido por um **sistema de autenticação** em que este atesta que o utilizador realizou uma autenticação bem-sucedida. Este *token* pode, então, ser repassado pelo utilizador para outras aplicações como uma prova de que o utilizador é quem diz ser. Estas aplicações devem ter a capacidade de validar os *tokens* recebidos para assegurar que o utilizador não forjou o *token*. Note que, em termos simplificados, a diferença entre um *token* e o autenticador usado na autenticação com *cookies* é simplesmente a procedência: em um, trata-se de um autenticador emitido por um sistema de autenticação separado, enquanto no outro, trata-se de um autenticador gerado internamente pela própria aplicação.

Resumidamente, uma autenticação com *tokens* funciona da seguinte forma:

1. A aplicação cliente (*i.e.*, o *browser*) acede à página de *login* do sistema de autenticação e fornece as suas credenciais. Possivelmente, este acesso ao sistema de autenticação é **resultado de um redirecionamento** feito pela aplicação *web* original resultante de uma tentativa do utilizador de aceder a algum recurso privilegiado.
2. O sistema de autenticação realiza a verificação das credenciais. Se a verificação é bem-sucedida, o sistema de autenticação inclui na sua resposta um *token*. Este *token* deve permitir que se verifique a sua autenticidade e integridade, bem como deve conter todas as informações sobre a identidade do utilizador necessárias à aplicação *web*.
3. A aplicação cliente realiza as requisições à aplicação *web*, **incluindo o *token***.
4. Ao receber uma requisição, a aplicação cliente **extrai o *token*, verifica sua autenticidade e a validade da autenticação** ali informada. Em caso de sucesso, a aplicação *web* dá acesso ao recurso requisitado, incluindo-o na resposta.

### Autenticadores JWT

Como o *token* é gerado pelo sistema de autenticação mas validado e lido pela aplicação *web*, é necessário que este siga um formato comum entendido por ambos os lados. Note, adicionalmente, que este *token* **não é apenas um identificador de sessão**: ao contrário, ele deve realmente conter uma série de informações relativas à identidade do utilizador e a validade / autenticidade do próprio *token*. Para garantir a interoperabilidade de *tokens* entre diversos sistemas de autenticação e diferentes aplicações *web*, o ideal é o uso de alguma solução *standard* que seja amplamente adotada. 

Neste contexto, surgem os autenticadores JWT (*JSON Web Token*). Trata-se de um formato padronizado pela RFC 7519 utilizado para representar *claims* (asserções) de maneira segura entre duas partes. Internamente, um JWT corresponde a um pequeno documento JSON estruturado segundo as regras definidas na RFC ao qual é adicionado um *header*. Posteriormente, sobre este conjunto (documento + *header*) é calculada uma marca de autenticidade (MAC ou assinatura digital), que é também anexada ao *token*. Opcionalmente, o *token* pode ser cifrado para obter confidencialidade dos seus dados internos.

Os detalhes do conteúdo de um JWT não são relevantes para esta aula (em aulas futuras, veremos os campos concretos utilizados em *tokens* JWT envolvidos nos protocolos OAuth 2.0 e OpenID Connect). Mas, apenas a título de ilustração, um *header* JWT (também chamado de *JOSE Header*) típico tem a seguinte composição:

```JSON
{
  "alg": "HS256",
  "typ": "JWT"
}
```

Neste *header*, há dois campos. O campo `alg` especifica qual esquema criptográfico foi utilizado para garantir as propriedades de segurança do *token*. Neste exemplo particular, o *token* utiliza o algoritmo HS256, que simplesmente denota que o *token* contém um MAC gerado a partir dum HMAC SHA-256, mas não foi cifrado. O segundo campo, denominado `typ`, simplesmente informa o tipo do documento JSON subjacente, neste caso um *token* JWT.

Por outro lado, um exemplo de payload JWT (também chamado de *JWT Claims Set*) pode ser visto abaixo:

```JSON
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022
}
```

Aqui, cada atributo refere-se a uma *claim*, ou seja uma asserção que o emissor do JWT faz a respeito do que quer que seja identificado pelo *token*. Por exemplo, o atributo `sub` contém um identificador único para o sujeito do JWT (*e.g.*, o utilizador que está a realizar a autenticação). O atributo `name` aqui fornece o nome do sujeito. Finalmente, o parâmetro `iat` define um selo temporal (*timestamp*) de quando o *token* foi emitido.

A RFC 7519 define que estes dois componentes, *header* e *payload*, devem ser codificados em Base64 antes de serem incluídos no *token*. Por exemplo, o *header* de exemplo acima codificado em Base64 resulta em:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
```

Já o *payload* tem a seguinte representação em Base64:

```
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ
```

Sobre estas representações, calcula-se o HMAC cujo resultado também é codificado em Base64, obtendo-se algo como (o valor exato depende da chave utilizada):

```
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

Ao final, estas três componentes são concatenadas (utilizando-se o carácter ponto como separador) numa única *string*, resultando no JWT final:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

!!! Abstract Demonstração
    **Objetivo:** mostrar a construção de um JWT utilizando ferramentas de linha de comando.

    **Execução:**

    - Começar pela codificação do *header* e do *payload* em Base64:

    ```bash
    $  echo -n '{"typ":"JWT","alg":"HS256"}' | basenc --base64url
    eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9
    $ echo -n '{"sub":"1234567890","name":"John Doe","iat":1516239022}' | basenc --base64url
    eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ==
    ```

    - Destacar que o padding deve ser removido.
    - Calcular o HMAC SHA-256 e codificar:

    ```bash
    $ echo -n eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ | openssl dgst -hmac minhachave -binary  | basenc --base64url
    0uMPyy-Ywsc0o0xInIhxSpoN8WA0JTDsozn_fw5thx4=
    ```

    - Juntar todas as componentes, obtendo:

    ```
    eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.0uMPyy-Ywsc0o0xInIhxSpoN8WA0JTDsozn_fw5thx4
    ```

#### Exemplo com JWT

Vejamos agora ver um exemplo simplificado de como uma aplicação *web* pode funcionar em conjunto com um sistema de autenticação através de *tokens* JWT. Aqui, assumimos que a aplicação web e o sistema de autenticação correspondem a servidores distintos e não relacionados. Particularmente, assumimos que a aplicação *web* tem endereço `https://www.secure-server.edu:4433`, enquanto o sistema de autenticação corre em `https://www.auth-server.edu:8433/`. 

Para este exemplo, vamos considerar uma hipotética aplicação bancária e, em específico, nos focaremos em um único recurso protegido chamado `/balance` (*i.e.*, permite consultar o saldo da conta do utilizador).

O início de uma possível implementação da *callback* para este recurso é mostrado a seguir:

```node
app.get("/balance", function (req, res) {

    var token = req.query.token;
    if (!token) {

        res.setHeader('Location',
            'https://www.auth-server.edu:8433/login?returnTo='
            + encodeURIComponent("https://www.secure-server.edu:4433/balance"));
        res.statusCode = 302;
        res.send("<html><body><h1>Private Area!</h1>Access blocked. Please, log in.</body></html>");

        return;
    }

    // Neste ponto, utilizador apresentou um token. Continua com a validação...
```

O trecho acima mostra que o código começa por tentar extrair o *token* a partir da *query string* da requisição: por simplicidade, aqui, iremos assumir que o *token* é fornecido como um parâmetro na URL solicitada. Em aulas futuras, veremos formas melhores de realizar o envio deste *token*.

Se o *token* não é encontrado na *query string*, isto significa que o utilizador não está autenticado. Portanto, é preciso solicitar que o mesmo se autentique junto ao sistema de autenticação antes de aceder aos recursos protegidos da aplicação.

Embora pudéssemos simplesmente apresentar uma mensagem ao utilizador a dizer que ele não se encontra autenticado, o mais comum nestas situações é que a aplicação automaticamente redirecione o utilizador ao sistema de autenticação. No código acima, fazemos isto ao retornar uma resposta com *status* 302. Lembre-se que este *status* do HTTP corresponde justamente a um redirecionamento. Mais especificamente, numa resposta de *status* 302, o servidor deve incluir um *header field* denominado `Location` que informa ao cliente a localização atual do recurso solicitado. 

Note que, no código, incluímos este *header field* com o valor `https://www.auth-server.edu:8433/login?returnTo=https://www.secure-server.edu:4433/balance`. Ou seja, redirecionamos o cliente para um recurso denominado `/login` no servidor do sistema de autenticação. Note, ainda, que a URL inclui um parâmetro denominado `returnTo` cujo valor corresponde a URL do recurso `/balance` no servidor da aplicação *web*. Como veremos mais adiante, este parâmetro servirá para que o sistema de autenticação faça o redirecionamento do cliente de volta à aplicação *web* após a autenticação.

Vejamos agora uma possível implementação da *callback* responsável por tratar das requisições `GET` ao recurso `/login` no sistema de autenticação:

```node
app.get("/login", function(req, res) {
    var form;

    form = '<form action="/login?returnTo=' + encodeURIComponent(req.query.returnTo) + '" method="POST">';
    form += '<label for="username">Username:</label><br>';
    form += '<input type="text" id="username" name="username" value=""><br>';
    form += '<label for="pass">Password:</label><br>';
    form += '<input type="password" id="pass" name="pass" value=""><br><br>';
    form += '<input type="submit" value="Submit">';
    form += '</form>';
    res.send("<html><body><h1>Login</h1>" + form + "</body></html>");
});
```

Pode-se notar que a *callback* simplesmente retorna um formulário contendo campos para o *username* e a *password*. A parte mais relevante deste trecho é o endereço especificado no atributo `action` do formulário. Este endereço diz ao *browser* a qual recurso do servidor os dados do formulário devem ser submetidos (neste caso, através de uma requisição `POST`). Em particular, neste exemplo o recurso é o próprio `/login`, mas note que o parâmetro `returnTo` é incluído neste caminho com o mesmo valor recebido na requisição atual.

Agora vejamos uma possível implementação para a *callback* que trata das requisições `POST` ao recurso `/login`:

```node
app.post("/login", function(req, res) {

    if (req.body.username != "admin" || req.body.pass != "123456") {

        res.send("<html><body>Invalid credentials. Try again.</body></html>");

        return;
    }

    authData = {user: req.body.username};
    token = jwt.sign(authData, key);

    res.setHeader('Location', req.query.returnTo + "?token=" + token);
    res.statusCode = 302;
    res.send("<html><body><h1>Login Successful</h1></body></html>");
});
```

Este trecho é bastante similar ao da *callback* correspondente no exemplo de autenticação com *cookies*. Inicialmente, são verificadas as credenciais do utilizador e, caso haja falha na validação, retorna-se uma mensagem de erro. 

As diferenças neste trecho começam justamente em caso de sucesso na validação das credenciais. Nesta hipótese, o código gera uma variável denominada `authData`, cujo objetivo é representar os dados do *token* a ser gerado. Para este exemplo simplificado, o *token* irá armazenar apenas o nome do utilizador, mas em sistemas reais mais informações são tipicamente incluídas.

Em seguida, transformamos o `authData` num *token* JWT propriamente dito. Para isto, aqui, utilizamos a biblioteca `jsonwebtoken` do NodeJS, mais especificamente seu método `sign`. Este método basicamente adiciona ao *payload* recebido (`authData`) um *header* JWT adequado e uma marca de autenticidade. Apesar do nome, o método pode tanto empregar uma assinatura digital quanto um esquema MAC. Aqui, em particular, a variável `key` denota uma chave simétrica e, portanto, o JWT contém um MAC.

De posse do conteúdo do JWT, a *callback* encerra ao gerar uma resposta com *status* 302 - um novo redirecionamento, portanto. Desta vez, o redirecionamento é para a URL indicada no parâmetro `returnTo` recebido pela *query string*. Lembre-se que este valor foi originalmente escolhido pela aplicação *web* e tratava-se do endereço do recurso protegido `/balance` ao qual o utilizador desejava aceder. Note, ainda, que a URL especificada no *header* `Location` é aumentada com um parâmetro denominado `token` cujo valor é a representação do JWT.

A consequência disto é que o cliente irá realizar uma nova requisição ao recurso `/balance` da aplicação *web*, mas que, desta vez, conterá o *token* JWT. Logo, voltando ao código da aplicação *web*, podemos ver o restante da implementação da *callback* responsável pelo recurso `/balance`:

```node
app.get("/balance", function (req, res) {

    // Check if there is a token in the query string
    //...
    //...

    // Token validation
    try {
        var decoded = jwt.verify(token, key);

        res.send("<html><body><h1>Private Area!</h1>User " + decoded.user + " has a balance of EUR " + currentBalance + "</body></html>");
    } catch(err) {

        res.setHeader('Location',
            'https://www.auth-server.edu:8433/login?returnTo='
            + encodeURIComponent("https://www.secure-server.edu:4433/balance"));
        res.statusCode = 302;
        res.send("<html><body><h1>Private Area!</h1>Invalid token. Please, log in.</body></html>");
    }
});
```

Após verificar que há, realmente, um *token* disponível, a aplicação necessita validá-lo. A validação mais fundamental é verificar se a marca de autenticidade do *token* está correta. Novamente, empregamos aqui a biblioteca `jsonwebtoken`, particularmente o método `verify`. Note que precisamos especificar uma chave que, neste exemplo, corresponde a uma chave simétrica partilhada com o sistema de autenticação.

Embora neste exemplo simplificado façamos apenas a validação da marca de autenticidade, em um sistema real provavelmente deveríamos fazer outras validações. Por exemplo, o *token* provavelmente deveria incluir um selo de tempo a indicar quando este foi criado, de forma que a aplicação *web* possa rejeitar *tokens* muito antigos. 

De toda forma, se o *token* passa às verificações, a aplicação procede ao processamento da requisição do utilizador, retornando o saldo da sua conta. Note, ainda, que a aplicação extrai informações úteis do *token*, como o nome de utilizador, por exemplo.

Por outro lado, caso a verificação do *token* falhe, a aplicação deve indicar ao utilizador que este precisa realizar (novamente) uma autenticação. Para tanto, podemos utilizar o mesmo artifício de redirecionamento empregado no caso de não haver um *token* na requisição.
