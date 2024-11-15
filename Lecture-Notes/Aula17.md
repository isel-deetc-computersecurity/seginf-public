# Aula 17 - OpenID Connect

Na última aula, estudamos o *framework* OAuth 2.0, que define como uma aplicação cliente (*client*) pode obter autorização para aceder a recursos protegidos de um *resource owner* localizados num *resource server*. O *framework* envolve, ainda, um *authorization server*, responsável pela emissão de um *access token*. O objetivo do *client* é obter um *access token* válido, o que lhe dá permissão de acesso aos recursos no *resource server*. Vimos, ainda, que o OAuth 2.0 prevê vários fluxos de autorização, *i.e.*, protocolos que definem as interações dos participantes de forma a permitir que o *client* obtenha o *access token*. Particularmente, nosso maior foco foi no fluxo *Authorization Code Grant*.

Embora o OAuth 2.0 permita que um *resource owner* autorize o *client* a aceder a determinados recursos, isto é feito sem fornecer ao *client* nenhuma informação sobre a identidade deste *resource owner*. Em outras palavras, o OAuth 2.0 provê autorização, mas não autenticação. 

Nesta aula, estudaremos o OpenID Connect. Trata-se de um ***standard* aberto** que objetiva permitir que uma aplicação cliente **autentique e obtenha informações adicionais** sobre um determinado utilizador junto a um **fornecedor de identidade** externo. Como veremos, o OpenID Connect é construído sobre o OAuth 2.0, adicionando sobre este uma camada de autenticação.

Em resumo, estudaremos como o OpenID Connect opera, o que ele adiciona sobre o OAuth 2.0 e veremos exemplos concretos de como criar uma aplicação cliente que utiliza este *standard*.

## OpenID Connect: Conceitos Básicos

Muito da operação do OpenID Connect já foi estudado na aula passada, por se tratar de uma extensão do OAuth 2.0. Abstratamente, podemos pensar no OpenID Connect como um *scope* adicional do OAuth 2.0 relativo à autenticação e a informações de identidade.

Lembre-se da última aula que, no OAuth 2.0, um *scope* define um limite para o conjunto de permissões que o *client* obtém sobre os recursos localizados no *resource server*. Assim, podemos pensar num serviço de autenticação de um *end-user* como um recurso específico disponível no *resource server*. Caso o *end-user* autorize, o *client* pode aceder a este recurso e obter informações sobre a sua identidade. 

Além disto, o OpenID Connect ainda introduz o conceito de *id token*. Como o nome sugere, um *id token* é o *token* que confirma a identidade do *end-user* de uma maneira verificável pela aplicação *client*. Este *id token* é gerado pelo *authorization server* e entregue ao *client* juntamente do *access token*. Por sua vez, o *access token* pode ser utilizado pelo *client* para que este aceda a uma API específica que fornece informações adicionais sobre o *end-user*. No contexto do OpenID Connect, estas informações são denominadas *User Info*.

## OpenID Connect: Papéis

Assim como no OAuth 2.0, o OpenID Connect define uma série de papéis e nomes específicos para os vários participantes envolvidos no processo de autenticação. Como veremos, alguns destes papéis são apenas nomes alternativos para entidades que já existem no OAuth 2.0. Mas, em alguns casos, veremos que não há um mapeamento perfeito entre o papel proposto pelo o OpenID Connect e os existentes no OAuth 2.0.

Um destes papéis é denominado *end-user*, e corresponde à entidade homônima no OAuth 2.0. Ou seja, trata-se do utilizador humano que deseja aceder a algum serviço de uma aplicação cliente. Entretanto, enquanto no OAuth 2.0 o *end-user* apenas autoriza a aplicação cliente a aceder a determinados recursos seus, no OpenID Connect o principal objetivo é permitir que o *end-user* autentique-se junto à aplicação cliente.

Como fica evidente do parágrafo anterior, um segundo papel é o da aplicação cliente, ou *client*. No entanto, no jargão do OpenID Connect, é comum utilizarmos o termo *Relying Party* (ou RP) para denotarmos esta aplicação. Este nome (livremente traduzido para algo como *a parte de confia*) advém do facto de que a aplicação cliente irá confiar na informação provida por uma terceira parte sobre qual é a identidade do utilizador (e de que se trata do utilizador autêntico). O principal objetivo da RP no OpenID Connect é, portanto, confirmar a identidade e autenticidade do *end-user* de forma a aceitar ou não prover a este um determinado serviço.

A terceira parte citada no parágrafo anterior, que fornece a informação de identidade e autenticidade do *end-user*, é denominada um **Fornecedor de Identidade** (do Inglês, *Identity Provider*, algumas vezes também denominado *OpenID Provider*). O fornecedor de identidade tem diversas responsabilidades, incluindo o armazenamento do registo do utilizador juntamente com as suas informações de validação de autenticação (*e.g.*, *password*) e o registo das aplicações cliente (*i.e.*, RPs) que pretendem autenticar utilizadores. Fazendo uma analogia entre o OpenID Connect e o *framework* básico do OAuth 2.0, o fornecedor de identidade opera como uma combinação entre o *authorization server* e o *resource server*: por exemplo, este mesmo elemento realiza tanto a geração do *access token* quanto, posteriormente, o envio das informações do utilizador para a aplicação cliente consoante a apresentação de um *access token* válido.

Para uniformizar e simplificar a notação utilizada no restante desta aula, deste ponto em diante utilizaremos a sigla RP para denotar a aplicação cliente (o *Relying Party*), o termo *end-user* para denotar o utilizador a ser autenticado e a sigla OP (do Inglês *OpenID Provider*) para denotar o fornecedor de identidade.

## OpenID Connect: Visão Geral

Abstratamente, o OpenID Connect opera de acordo com o seguinte diagrama:

```
+--------+                                   +--------+
|        |                                   |        |
|        |---------(1) AuthN Request-------->|        |
|        |                                   |        |
|        |  +--------+                       |        |
|        |  |        |                       |        |
|        |  |  End-  |<--(2) AuthN & AuthZ-->|        |
|        |  |  User  |                       |        |
|   RP   |  |        |                       |   OP   |
|        |  +--------+                       |        |
|        |                                   |        |
|        |<--------(3) AuthN Response--------|        |
|        |                                   |        |
|        |---------(4) UserInfo Request----->|        |
|        |                                   |        |
|        |<--------(5) UserInfo Response-----|        |
|        |                                   |        |
+--------+                                   +--------+
```

O processo inicia-se com o envio, por parte do RP, de uma requisição ao OP. Note que este diagrama é meramente conceitual e que, na prática, este envio de requisição pode não ser feito diretamente do RP ao OP, mas sim através de uma redireção do *user-agent* do *end-user* (utilizando a terminologia introduzida no final da última aula, esta comunicação utilizaria o *Front Channel*).

De seguida, o OP procede a autenticação do *end-user*. Se estar for bem-sucedida, adicionalmente, o OP deve obter a autorização do *end-user* para particular suas informações com o RP que iniciou a solicitação.

O próximo passo é o envio, por parte do OP, de um *id token* e (possivelmente) de um *access token* ao RP. Novamente, este envio pode não ocorrer diretamente (*i.e.*, através do *Back Channel*), mas sim utilizando o *Front Channel*.

Ao receber os *tokens*, o RP pode, opcionalmente, realizar uma requisição a um recurso específico do OP denominado de *UserInfo end-point* apresentando o *access token*. Em caso de sucesso, o OP retorna como resposta a *User Info* associada ao *end-user*. Ao contrário das demais interações entre RP e OP, esta troca de requisição e resposta é realizada pelo *Back Channel* e, portanto, não envolve o *user-agent* do *end-user*.

## OpenID Connect: Fluxo *Authorization Code*

Lembre-se que o OAuth 2.0 prevê diversos fluxos de autorização distintos. Logo, por ser uma extensão do OAuth 2.0, o OpenID Connect também suporta vários fluxos. No entanto, para os propósitos desta UC, focaremos apenas no fluxo *Authorization Code*, que corresponde ao *Authorization Code Grant* do OAuth 2.0.

Um resumo deste fluxo pode ser visto no diagrama de sequência abaixo:

```
User-agent                                        RP                                          OP                   
   │                                               │                                           │                   
   │             ┌──────────────┐                  │                                           │                   
   ┼─────────────│ Login start  │ ────────────────►│                                           │                   
   │             └──────────────┘                  │                                           │                   
   │                                               │                                           │                   
   │       ┌────────────────────────────────┐      │                                           │                   
   │       │ 302 Redirect para Autorization │      │                                           │                   
   │◄──────│                                │──────┼                                           │                   
   │       │     endpoint +scope=openid     │      │                                           │                   
   │       └────────────────────────────────┘      │                                           │                   
   │                    ┌──────────────────────────┼───────────────┐                           │                   
   │                    │                          │               │                           │                   
   ┼────────────────────┼ GET authorization─endpoint +scope=openid ┼──────────────────────────►│  ┌───────────────┐
   │                    │                          │               │                           │  │ Authorization │
   │                    └──────────────────────────┼───────────────┘                           │  │               │
   │                                               │                                           │  │    endpoint   │
   │                     ┌─────────────────────────┴──────────────┐                            │  └───────────────┘
   │◄────────────────────┤302 Redirect para URL da callback + code├────────────────────────────┼                   
   │                     └─────────────────────────┬──────────────┘                            │                   
   │                                               │                                           │                   
   │                                               │                                           │                   
   │         ┌───────────────────┐                 │                                           │                   
   ┼─────────┤GET callback + code├────────────────►│                                           │                   
   │         └───────────────────┘                 │    ┌──────────────────────────────────┐   │                   
   │                                               │    │          POST /token             │   │                   
   │                                               ┼────┼                                  ┼──►│                   
   │                                               │    │+ code + client_id + client_secret│   │  ┌──────────┐     
   │                                               │    └──────────────────────────────────┘   │  │  Token   │     
   │                                               │                                           │  │          │     
   │                                               │                                           │  │ endpoint │     
   │                                               │         ┌────────────────────────┐        │  └──────────┘     
   │                                               │◄────────┤Access Token + ID Token─┼────────┼                   
   │                ┌─────────┐                    │         └────────────────────────┘        │                   
   │◄───────────────┤Login end├────────────────────┼                                           │                   
   │    ┌───────────┴─────────┴────────────┐       │                                           │                   
   │    │Autenticador (e.g., userid + hmac)│       │      ┌─────────────────────────────┐      │                   
   │    └──────────────────────────────────┘       ┼──────┤GET /user_info + access_token├─────►│  ┌──────────┐     
   │                                               │      └─────────────────────────────┘      │  │ UserInfo │     
   │                                               │                                           │  │          │     
   │                                               │                ┌─────────┐                │  │ endpoint │     
   │                                               │◄───────────────┤User Info├────────────────┼  └──────────┘     
   │                                               │                └─────────┘                │                   
   │                                               │                                           │                   
                                                                                               │                   
                                    ┌──────────────────────────────┐                                               
                                    │ Uid: access token / id token │                                               
                                    │                              │                                               
                                    │          / user info         │                                               
                                    └──────────────────────────────┘                                                            
```

O fluxo começa por iniciativa do *end-user* (no diagrama, representado pelo seu *user-agent*, denominado simplesmente "*Browser*" na Figura). Este **solicita um processo de *login* junto à aplicação cliente** (a RP). Para isto, o *user-agent* acede a um determinado recurso da RP relativo a *login* (e.g., `/login`).

No entanto, recorde que o propósito do OpenID Connect é justamente delegar o processo de autenticação para o fornecedor de identidade (o OP). Assim, ao receber a requisição no seu *end-point* de *login*, a RP gera como resposta um redirecionamento do *user-agent* para o ***authorization end-point*** do *authorization server* do OP.

Note que este redirecionamento é análogo ao realizado no início do fluxo *Authorization Code Grant* do OAuth 2.0. Mais especificamente, a RP deve anexar à URI do *authorization end-point* uma série de parâmetros, incluindo os parâmetros `response_type` (aqui, com valor `code`), `client_id`, `state` e `scope`, todos com significados e valores idênticos aos que seriam utilizados num fluxo de autorização OAuth 2.0. O detalhe importante aqui é que o parâmetro `scope` **deve, necessariamente, incluir o *scope* `openid`**, indicando que a RP deseja ter acesso às informações de identificação do *end-user*. Um exemplo de resposta HTTP correspondente a um redirecionamento para o *authorization end-point* pode ser visto abaixo:

```http
HTTP/1.1 302 Found
Location: https://server.example.com/authorize?
    response_type=code
    &scope=openid%20profile%20email
    &client_id=s6BhdRkqt3
    &state=af0ifjsldkj
    &redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb
```

Quando o *user-agent* segue o redirecionamento e envia a requisição ao *authorization end-point* do OP, o que se segue é um processo de autenticação. Ou seja, de alguma forma, o OP deve solicitar as credenciais de autenticação do *end-user* e validá-las. Assim como a especificação do OAuth 2.0, a especificação do OpenID Connect não define como este process é realizado, mas, tipicamente, isto envolve a apresentação de uma interface com um formulário onde se solicita, por exemplo, um *username* e *password*. 

Seja como for, após uma autenticação bem-sucedida do *end-user*, o OP procede a uma etapa de **obtenção do consentimento do *end-user***. Ou seja, o OP deve informar ao *end-user* que aquela RP específica está a requisitar acesso às suas informações de identidade e solicitar a aprovação (ou não) da autorização de acesso.

Ao final deste processo de autenticação e consentimento, o *authorization server* do OP realiza o redirecionamento do *user-agent* do *end-user* de volta para a RP. Assim como especificado pelo OAuth 2.0, este redirecionamento é feito em direção à ***callback* URL**, ou seja, à URL especificada pela RP no parâmetro `redirect_uri` do redirecionamento anterior. A esta URL, o OP **anexa o código de autorização** gerado como consequência do consentimento do *end-user*.

Este redirecionamento faz com que o *user-agent* envie uma requisição HTTP para a RP, efetivamente entregando-lhe o código de autorização.

De posse deste código, a RP acede ao ***token end-point*** do *authorization server*. Nesta requisição, são incluídos o código de autorização e as credenciais da RP junto ao servidor (nomeadamente, seu `client_id` e seu `client_secret`). Note que esta requisição é realizada pelo ***Back Channel***, ou seja, trata-se de uma **comunicação direta entre RP e OP**. Um exemplo de possível requisição ao *token end-point* seria:

```http
POST /token HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW

grant_type=authorization_code&code=SplxlOBeZQQYbYS6WxSbIA
&redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb
```

Note que esta requisição segue exatamente as mesmas especificações definidas pelo OAuth 2.0. Por exemplo, as credenciais da RP são fornecidas utilizando o *Basic Authentication* do HTTP.

Ao receber tal requisição, o *authorization server* do OP verifica a validade do código de autorização, das credenciais da RP e da consistência das demais informações (por exemplo, de se aquele código foi gerado para aquela RP em específico). Se todas as verificações são bem-sucedidas, **o servidor gera um *id token* e, possivelmente, um *access token***.

Conforme citado brevemente no início desta aula, o *id token* é um *token* especial que serve para confirmar a identidade legítima do *end-user* junto à RP. Ao contrário do *access token*, no entanto, que é opaco, o *id token* tem um formado *standard* que pode (e deve) ser interpretado pela RP. Mais precisamente, um ***id token* corresponde a um JWT (*JSON Web Token*) assinado pelo OP**. O *payload* deste *id token*, por sua vez, contém uma série de **asserções** sobre o *end-user*. 

Um exemplo hipotético de resposta gerada pelo OP a uma requisição direcionada ao seu *token end-point* pode ser visto a seguir (adaptado da especificação do OpenID Connect 1.0):

```http
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-store

{
"access_token": "SlAV32hkKG",
"token_type": "Bearer",
"refresh_token": "8xLOxBtZp8",
"expires_in": 3600,
"id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.ewogImlzc
    yI6ICJodHRwOi8vc2VydmVyLmV4YW1wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5
    NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUzZ
    fV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5Nz
    AKfQ.ggW8hZ1EuVLuxNuuIJKX_V8a_OMXzR0EHR9R6jgdqrOOF4daGU96Sr_P6q
    Jp6IcmD3HP99Obi1PRs-cwh3LO-p146waJ8IhehcwL7F09JdijmBqkvPeB2T9CJ
    NqeGpe-gccMg4vfKjkM8FcGvnzZUN4_KSP0aAp1tOJ1zZwgjxqGByKHiOtX7Tpd
    QyHE5lcMiKPXfEIQILVq0pc_E2DzL7emopWoaoZTF_m0_N0YzFC6g6EJbOEoRoS
    K5hoDalrcvRYLSrQAZZKflyuVCyixEoV9GfNQC3_osjzw2PAithfubEEBLuVVk4
    XUVrWOLrLl0nx7RkKU8NXNHq-rvKMzqg"
}
```

Note como o corpo da resposta corresponde a um JSON, contendo diversos campos. A maior parte destes campos são idênticos aos encontrados na resposta análoga definida pela especificação do OAuth 2.0 (*e.g.*, `access_token`, `token_type`, `refresh_token`). No entanto, há uma entrada específica denominada `id_token` que contém um JWT assinado digitalmente pelo OP. Ao descodificarmos este JWT, encontramos como payload o seguinte JSON:

```JSON
{
  "iss": "http://server.example.com",
  "sub": "248289761001",
  "aud": "s6BhdRkqt3",
  "nonce": "n-0S6_WzA2Mj",
  "exp": 1311281970,
  "iat": 1311280970
}
```

Embora a especificação do OpenID Connect preveja outros possíveis tipos de *claims*, este exemplo ilustra as mais comuns:

- `iss`: identifica o emitente do *token*, na forma de uma URL HTTPS.
- `sub`: identifica o sujeito, ou seja, o *end-user* que se autenticou junto ao OP. O valor deste campo deve ser um identificador único para aquele utilizador naquele OP. Embora aqui seja mostrado na forma de um número, o OpenID Connect define que este identificador pode ser qualquer *string* com menos que 256 caracteres ASCII. A especificação também define que esta *string* é *case-sensitive*: por exemplo, `"abcd"` corresponde a um identificador diferente de `"ABCD"`.
- `aud`: identifica a **audiência** do *token*. Em outras palavras, trata-se de um identificador da entidade para a qual o *token* foi gerado. A audiência deve sempre incluir a RP (especificamente, o seu `client_id`), mas a especificação do OpenID Connect deixa aberta a possibilidade de mais identificadores serem adicionados ao valor deste campo, indicando que a audiência do *token* inclui outras entidades.
- `nonce`: analogamente aos *nonces* utilizados por outros protocolos criptográficos estudados nesta UC, trata-se de um valor aleatório que tem por objetivo evitar ataques de repetição. Neste caso, a preocupação é evitar que um atacante se passe pelo OP e envie uma cópia de um `id_token` legítimo gerado no passado. Para que o RP saiba que não se trata de uma repetição, ele pode incluir um *nonce* no redirecionamento utilizado para enviar o *user-agent* ao `authorization end-point`. Neste caso, o *authorization server* é obrigado a armazenar este *nonce* e, posteriormente, incluí-lo nesta *claim* do *id token*. Assim, o RP pode verificar se os dois valores de *nonce* são iguais, evitando os ataques de repetição.
- `exp`: informa uma data de expiração para este *token*. Após a data informada, o RP não deve mais considerar válidas as informações contidas no *token* para propósitos de autenticação do *end-user*. Neste caso, será necessário iniciar um novo fluxo para re-autenticar o utilizador. A data é representada como uma quantidade de segundos passados desde 1 de janeiro de 1970 (uma data de referência conhecida como *Unix epoch*).
- `iat`: informa a data em que este *token* foi emitido. Assim como o campo `exp`, é representado pelo número de segundos decorridos desde a *Unix epoch*.

A exceção da *claim* *nonce*, que é obrigatória apenas caso o RP tenha incluído um *nonce* no seu redirecionamento do *user-agent* ao *authorization end-point*, todas as outras *claims* ilustradas aqui são sempre obrigatórias. Além destas, o OpenID Connect prevê outras opcionais na sua especificação, além de permitir que implementações particulares incluam outras próprias.

De volta às etapas do fluxo, ao receber a resposta do *token end-point*, a RP realiza a verificação da assinatura do *token* e da consistência das *claims* lá contidas. Assumindo sucesso nestas validações, uma aplicação cliente típica irá considerar o *end-user* agora autenticado, criando para ele uma sessão armazenada num repositório de sessões ativas. Associados a esta sessão, serão armazenados o *id token* e o *access token*, se houver.

Ao fim deste processo, a RP finalmente envia uma resposta ao *user-agent* informando sobre a conclusão da operação de *login*. Lembre-se que o código de autorização gerado pelo *authorization end-point* foi repassado ao RP através de um redirecionamento do *user-agent* para a `redirect_uri` do RP. Esta resposta é justamente a resposta HTTP correspondente à requisição causada por este redirecionamento. Além da informação sobre a conclusão do processo, esta resposta também transporta algum tipo de autenticador (por exemplo, num *cookie*) incluindo o identificador da sessão recém-criada. 

### O *UserInfo end-point*

Embora não seja obrigatório, segundo a especificação, é **bastante comum** que implementações do OpenID Connect incluam no OP um recurso denominado *UserInfo end-point*. O propósito deste recurso é permitir à RP obter informações adicionais sobre o *end-user*, além daquelas já fornecidas pelo *id token*. 

Para aceder ao *UserInfo end-point*, a RP necessita do *access token*. Logo, este recurso funciona de maneira análoga a qualquer outro recurso protegido do *end-user* num *resource server*. Particularmente, a RP deve incluir o *access token* recebido juntamente do *id token* num *header field* `Authorization` da requisição, como exemplificado abaixo:

```http
GET /userinfo HTTP/1.1
Host: server.example.com
Authorization: Bearer SlAV32hkKG
```

Note que o tipo de autorização aqui é o *Bearer Token*: embora o OAuth 2.0 aceite outros tipos de *access token*, o OpenID Connect exige o uso de *tokens* do tipo *bearer*.

Ao receber uma requisição ao *UserInfo end-point* com um *access token* válido, o OP retorna um objeto do tipo *UserInfo* na forma de um JSON. Opcionalmente, este *UserInfo* pode ser cifrado e/ou assinado pelo OP, sendo neste caso um JWT.

As *claims* contidas no *UserInfo* podem ser diversas. A especificação do OpenID Connect apresenta uma lista com 20 possibilidades, incluindo informações como endereços de e-mail, género do utilizador, data de aniversário, endereço e fotografia. No entanto, implementações específicas não são obrigadas a fornecer todas estas informações. Adicionalmente, a especificação permite que implementações específicas adicionem *claims* diferentes destas 20. Apenas a título de ilustração, o JSON a seguir mostra um *UserInfo* gerado pelo OP da Google para um utilizador hipotético:

```JSON
{
"family_name": "Surname",
"name": "Alice",
"picture": "…",
"email": alice@gmail.com
"gender": "female",
"link": "https://plus.google.com/...",
"given_name": "Alice",
"id": "100...2243139"
}
```

É comum que RPs solicitem o *UserInfo* do *end-user* tão logo recebam o *access token*. Além disto, o *UserInfo* recebido também é tipicamente armazenado junto das demais informações acerca do *end-user* no repositório de sessões ativas.

## Múltiplos *Scopes*

Como é possível notar, o fluxo *Authorization Code* do OpenID Connect tem a mesma estrutura do fluxo *Authorization Code Grant* do OAuth 2.0. As diferenças notáveis são a utilização do *scope* `openid` e a inclusão do *id token*, recebido ao lado do *access token*. Por este motivo, o OpenID Connect suporta que sejam combinados os processos de autenticação do *end-user* junto à RP com a obtenção de autorização, por parte do *client*, para aceder a recursos protegidos do *end-user*.

Para ilustrar esta ideia, consideremos novamente o exemplo concreto do início da aula passada: a Alice, um *end-user*/*resource owner*, tem fotografias (recursos protegidos) armazenados num serviço denominado `myphotos.com` e deseja imprimi-las utilizando uma aplicação cliente / RP denominada `printonline.com`. Suponha que, além do acesso às fotos a serem impressas, a `printonline.com` necessite também obter autenticar a Alice e obter algumas informações básicas sobre ela (por exemplo, seu endereço de e-mail). Agora suponha que o serviço `myphotos.com`, além de dar suporte a autorização via OAuth 2.0, atue como um OP, utilizando o OpenID Connect. Quando a Alice acede ao `printonline.com` e solicita o serviço de impressão, pode ser interessante a esta aplicação realizar, num único fluxo, a autenticação da Alice, a obtenção dos seus dados e a obtenção da autorização para aceder às fotos.

Este objetivo é completamente compatível com o fluxo *Authorization Code* do OpenID Connect. Para tanto, basta que o *client* / RP, ao realizar o redirecionamento do *user-agent* para o *authorization end-point*, inclua no parâmetro `scope` tanto o *scope* `openid` (para autenticação e acesso às informações do utilizador) quando o *scope* que autoriza o acesso às fotos da Alice. Neste caso, o *access token* entregue pelo *authorization server* ao *client* / RP codificará que este tem autorização para aceder tanto ao *UserInfo end-point* quando ao recurso localizado no *resource server* referente às fotografias.

De facto, esta é uma abordagem bastante comum: grandes fornecedores de identidade na Internet são integrados a APIs diversas que permitem a manipulação dos dados dos seus utilizadores, de forma que aplicações cliente podem simultaneamente realizar a autenticação do *end-user* (via OpenID Connect), a obtenção das suas informações (via UserInfo) e a autorização de acesso aos recursos (via OAuth 2.0). Exemplos de fornecedores de identidade que realizam esta integração incluem a Google e o Github.
