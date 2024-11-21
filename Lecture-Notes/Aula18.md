# Aula 18 - Ataques contra o OAuth 2.0 e Contramedidas

Nas duas últimas aulas, estudamos o *framework* OAuth 2.0 e o OpenID Connect -- este último uma solução de autenticação construída sobre o OAuth 2.0. Embora tenhamos visto em detalhes o funcionamento destes protocolos, o propósito de certos parâmetros e ações neles incluídos podem não ser evidentes. Em certos casos, alguns destes parâmetros e ações podem parecer desnecessários ao objetivo final dos protocolos. Exemplos incluem o uso do parâmetro **state** e a necessidade da especificação da `redirect_uri` durante o processo de registo de uma aplicação cliente junto ao *authorization server*. Em geral, tais detalhes foram incluídos nestes protocolos com o propósito de fornecer proteção contra possíveis tentativas de ataque, embora possa não parecer óbvio como tal proteção se dá. 

Nesta aula, discutiremos alguns possíveis ataques contra o OAuth 2.0 e veremos como recursos do protocolo funcionam como uma linha de defesa contra estes. Daremos especial ênfase a recomendações que devem ser observadas em implementações do OAuth 2.0. Note que, dado que o OpenID Connect é definido sobre o OAuth 2.0, estes ataques, proteções e recomendações aplicam-se igualmente a ele.

## Hipóteses sobre o Atacante

Sempre que analisamos a segurança de um determinado sistema informático, é fundamental o estabelecimento de um **modelo de atacante**. Este modelo estabelece as características do atacante, incluindo, nomeadamente, as capacidades e recursos do mesmo. Ou seja, **o que o atacante é capaz de realizar** (*e.g.*, o atacante pode monitorizar o canal de comunicação? Pode alterar bits nas mensagens em trânsito? Tem a capacidade de enviar uma mensagem passando-se por outra entidade?). 

A partir deste modelo, podemos formular ataques em concreto que tal atacante pode tentar perpetrar sobre o sistema. Podemos também analisar sob quais condições o sistema é vulnerável a estes ataques e, equivalentemente, sob quais condições o sistema é imune aos mesmos.

Nesta aula, ao analisarmos o OAuth 2.0, consideraremos o modelo de atacante descrito na RFC 6819. Particularmente, este modelo assume que:

1. O atacante tem **total acesso à rede de comunicação entre o *client* e o *authorization server* e entre o *client* e o *resource server***. Isto significa que o atacante pode, entre outras coisas, monitorizar qualquer comunicação entre estas partes (*i.e.*, ver o conteúdo das mensagens trocadas). No entanto, assume-se que o atacante **não tem acesso às comunicações entre o *authorization server* e o *resource server***.
2. O atacante tem recursos ilimitados para dar suporte ao ataque. Isto significa que devemos assumir que o atacante pode, por exemplo, configurar servidores maliciosos ou dispositivos que atuem como falsos *resource owners*.
3. Assume-se, também, que duas das três partes envolvidas no protocolo (*client*, *resource owner*, *authorization server*) podem atuar em **conluio**. Ou seja, estas partes podem cooperar umas com as outras de forma a tentar, em conjunto, quebrar a segurança do protocolo. Como um exemplo mais concreto, devemos assumir que tanto o *client* quanto o *authorization server* podem estar sob controlo do atacante e atuar em conluio para tentar obter acesso a recursos protegidos do *resource owner*.

## Possíveis Ataques

Com base no modelo de atacante apresentado na seção anterior, a RFC 6819 lista uma grande quantidade de possíveis ameaças (a volta de 50). Nesta UC, nos concentraremos em um pequeno subconjunto destas, particularmente nas que envolvem ataques do tipo *Cross-Site Request Forgery*, estudado em aulas anteriores, e nas ameaças específicas ao fluxo *Authorization Code Grant*. No entanto, discutiremos também de forma mais superficial ameaças gerais contra elementos específicos do OAuth 2.0. 

### Ameaças Contra a Aplicação *Client*

As principais ameaças contra o *client* dizem respeito a tentativas de comprometimento do `client_secret`. De posse do `client_secret` de uma aplicação legítima, um atacante pode tentar realizar ataques posteriores baseados na repetição de *refresh tokens* e ou de códigos de autorização. Em outras palavras, o atacante pode monitorizar as comunicações entre o *client* e o *authorization server* e depois tentar repetir as mensagens enviadas pelo *client* com o propósito de tentar obter *access tokens* válidos. Outra hipótese é que o atacante utilize o `client_secret` para tentar impersonar a aplicação legítima, levando o *resource owner* a dar permissão de acesso a recursos protegidos e o *authorization code* a emitir *access tokens* válidos.

Mas como um atacante conseguiria comprometer o `client_secret` de uma aplicação *client* legítima? Existem diversas possibilidades, resultantes de erros na gestão das credenciais por parte dos desenvolvedores da aplicação. Por exemplo, um erro comum é a inclusão do valor do `client_secret` no código-fonte da aplicação. Se a aplicação é *open source*, então qualquer atacante pode inspecionar seu código-fonte e obter o `client_secret`. Ainda que a aplicação seja *closed source* e o atacante não tenha acesso ao código-fonte, o facto de o `client_secret` ser incluído diretamente no código faz com que seu valor fique registado de alguma forma nos ficheiros binários da distribuição da aplicação.

> [!NOTE]
>
>    Ilustração de como um `client_secret` (ou outra credencial similar) pode ser facilmente extraído de um ficheiro binário executável de uma aplicação.
>
>
>    Considere o seguinte código de um hipotético programa que faça a manipulação de um `client_secret`:
>
>    ```C
>    #include <string.h>
>
>    #define CLIENT_SECRET	"segredo_muito_seguro"
>
>    int main() {
>
>        char * client_secret = CLIENT_SECRET;
>
>        ////
>        // Procede com os passos do protocolo.
>        // ...
>        //
>        
>        return(strlen(client_secret));
>    }
>    ```
>
>    - Compilar o programa:
>
>    ```bash
>    $ gcc client_secret_inseguro.c -o client_secret_inseguro
>    ```
>
>    - Inspecionar o ficheiro binário resultante em busca do `client_secret`. No Linux, pode ser utilizado o comando `strings` que busca *strings* de caracteres ASCII em ficheiros binários:
>
>    ```bash
>    $ strings ./client_secret_inseguro | grep segredo
>    segredo_muito_seguro
>    ```
>
>   Por se tratar de uma constante utilizada no código, o valor de `CLIENT_SECRET` necessita ser armazenado em algum ponto do executável. Ainda que se trate de um ficheiro binário, o *client secret* encontra-se em texto plano no executável, como ilustra a demonstração.

Logo, para garantir a segurança do armazenamento do `client_secret` é necessário que o mesmo seja armazenado fora do código-fonte ou de outros recursos da aplicação que resultem em artefactos do *software* distribuídos pelo desenvolvedor. Na prática, isto significa que as **credenciais do *client* devem ser específicas de cada ambiente de implantação do *software***. Em outras palavras, para **cada ambiente** em que aquele *software* em particular é **instalado**, deve ser registada **uma nova aplicação** junto ao *authorization server*, gerando-se como consequência um par específico de credenciais fornecidas ao *software* por algum meio (por exemplo, num **ficheiro de configuração**).

Mesmo que estes cuidados sejam tomados, é importante que também sejam utilizadas medidas gerais de proteção ao ambiente onde corre a aplicação. Por exemplo, caso a aplicação corra num servidor partilhado por múltiplos utilizadores, é importante garantir que apenas o utilizador que corre a aplicação tenha permissão para leitura do ficheiro de configuração que contenha o `client_secret`. Outras medidas gerais de proteção contra acessos não autorizados ao servidor (*e.g.*, utilização de palavras-passe fortes) também são necessárias.

Outro ponto importante é que, mesmo que todas as precauções sejam tomadas, é sempre importante antecipar a possibilidade de mesmo assim o `client_secret` ser eventualmente comprometido. Neste caso, o sistema deve ter mecanismos para mitigar os efeitos deste comprometimento. No caso específico do OAuth 2.0, deve ser possível revogar o `client_secret` de um *client*. Ou seja, caso se saiba -- ou se tenha motivo para crer -- que o `client_secret` foi comprometido, o *authorization server* deve ter um mecanismo que permita a revogação (possivelmente, seguida da emissão de um novo segredo).

Considerações similares aplicam-se também aos *refresh tokens*. Caso um atacante tenha acesso aos *refresh tokens* emitidos para um *client* legítimo, ele pode tentar realizar a troca destes por *access tokens* junto ao *authorization server*. Contramedidas aplicáveis a esta situação incluem garantir que é utilizada uma autenticação forte do *client* junto ao *authorization server* (de modo que o atacante apenas possuir os *refresh tokens* não seja suficiente para trocá-los por *access tokens*) e ter mecanismos para a revogação dos *refresh tokens*, em caso de comprometimento dos mesmos.

Igualmente, aplicações *client* devem ter cuidado no armazenamento e manipulação dos *access tokens*. Estes são particularmente problemáticos porque frequentemente são do tipo *bearer* e o *resource server* baseia a autenticação na simples posse do *access token*. Isto significa que um atacante que consegue obter um *access token* válido pode, nestas condições, trivialmente aceder aos recursos protegidos do *resource owner*.

Desta forma, é importante que  as aplicações *client* evitem armazenar *access tokens* em elementos de memória que possam ser acessados por outras aplicações (por exemplo, ficheiros sem permissões apropriadas). Além disto, é importante tomar medidas para que um atacante não possa extrair o *access token* quando este é transmitido do *authorization server* para o *client*. Isto significa, na prática, o uso de TLS nas comunicações entre estas entidades. Por fim, da perspetiva da mitigação dos efeitos de um eventual comprometimento de um *access token*, é importante limiar o escopo das autorizações associadas a cada *token* ao mínimo necessário para o funcionamento do *client*. Outra recomendação é manter a validade dos *access tokens* relativamente pequena, de forma a minimizar a probabilidade de que um *access token* comprometido possa ser utilizado pelo atacante.

Outro risco associado ao *client* é que ele próprio seja malicioso. Por exemplo, um *client* malicioso pode ter o objetivo de obter as credenciais (*username* + *password*) do *resource owner*. Para tanto, uma possibilidade é que o *client*, ao invés de redirecionar o *user-agent* para o *authorization end-point*, o redirecione para uma réplica deste localizado no próprio servidor da aplicação, induzindo o *end-user* a fornecer suas credenciais ao *client*, imaginando que se trate do *authorization server*. Isto corresponde a um ataque clássico denominado *phishing*. Um agravante deste ataque é que o *client* pode empregar uma série de artifícios para mascarar o facto de que o *authorization end-point* não é o legítimo (por exemplo, possa apresentá-lo num `iframe`, de forma que o *end-user* não possa verificar a URL da página na barra de endereços do *browser*).

Contramedidas para este ataque incluem estratégias de validação de aplicações *client* antes da sua publicação e a educação dos utilizadores para sinais de *phishing*. No entanto, como a própria RFC 6819 nota, o OAuth 2.0 não inclui mecanismos de proteção nativos contra aplicações *client* maliciosas.

### Ameaças Contra o *Authorization End-Point*

Lembre-se que o *Authorization End-Point* é o recurso do *authorization server* para o qual o *user-agent* do *resource owner* é redirecionado pelo *client*. Na interação com este recurso, o *resource owner* pode ser obrigado a fornecer suas credenciais de autenticação, pelo que se trata de um ponto sensível do fluxo.

Neste sentido, um possível ataque é um *phishing* através da falsificação do *authorization server*. Em outras palavras, o atacante pode criar uma réplica da interface de autenticação do *authorization end-point* num servidor do seu controlo. Se o atacante for capaz de fazer com que o *user-agent* comunique-se com o seu servidor ao invés do legítimo, o *resource owner* pode ser induzido a relevar sua *password*. A depender das capacidades do atacante, isto pode ser alcançado através de técnicas como a manipulação de entradas do servidor DNS local da rede da vítima, por exemplo.

Tal ataque pode ser coibido através de certificados digitais. Ou seja, contanto que seja utilizado TLS/HTTPS nesta comunicação entre o *user-agent* e o *authorization end-point*, o *user-agent* deve ser capaz de detetar que não se trata do servidor legítimo e alertar o utilizador.

Outro potencial ataque pode envolver um *client* malicioso que requisita mais *scopes* que o necessário para realizar a tarefa a qual a aplicação propõe-se. Por exemplo, digamos que a aplicação necessite apenas de acesso para leitura de determinados ficheiros do *resource owner*, mas inclua também no parâmetro `scope` a solicitação de permissões para escrita. Neste caso, ao obter a autorização, a aplicação poderia remover ou criar ficheiros no armazenamento do *resource owner*.

Este ataque pode ser mitigado através da apresentação pelo *authorization server* de uma explicação clara e suficientemente detalhada sobre as autorizações que o *client* requisitou. É também necessário educar os utilizadores para que eles deem a devida atenção a esta informação. Além disto, numa contramedida que depende menos do envolvimento dos utilizadores, o próprio *authorization server* pode classificar os *clients* registados de acordo com o seu nível de confiança e com base nisto fazer uma pré-seleção dos *scopes* os quais cada *client* tem direito de solicitar.

### Ameaças Contra o *Token End-Point*

Da perspetiva do *token end-point*, um atacante pode ter dois objetivos gerais: obter *access tokens* válidos para aceder a recursos de determinados *resource owners* ou obter as credenciais de *clients* legítimos para, posteriormente, impersoná-los. Lembre-se que o *token end-point* realiza a autenticação do *client* e, portanto, necessita armazenar alguma informação de validação das credenciais. Igualmente, este *end-point* é responsável pela geração dos *access tokens*, pelo que pode eventualmente armazená-los numa lista de *access tokens* ativos.

Desta forma, se um atacante consegue, de alguma maneira, aceder ao armazenamento interno do *authorization server*, ele pode ser capaz de recuperar credenciais de autenticação de *clients* legítimos ou *access tokens* válidos a partir de algum ficheiro ou base de dados.

Para coibir estes ataques, é necessário empregar corretamente políticas de segurança que evitem acessos indevidos às bases de dados e ficheiros manipulados pelo *token end-point*. Além disto, é importante evitar o armazenamento direto das credenciais dos *clients* e dos *access tokens* gerados. Ao invés disto, estas credenciais devem ser armazenadas seguido as melhores práticas, o que inclui, por exemplo, o armazenamento apenas de *hashes* criptográficos das mesmas.

Outra abordagem que um atacante pode tentar empregar é a monitorização da comunicação entre o *client* e o *token end-point*. Como nesta comunicação são trocadas mensagens que contêm tanto as credenciais do *client* quanto os *access tokens* gerados, um atacante pode, hipoteticamente, conseguir extrair tais informações. 

A contramedida, neste caso, é o simples uso de TLS/HTTPS para estas comunicações. Neste caso, a comunicação será criptografada, pelo que assume-se que o atacante não consiga decifrar as mensagens. 

Há, ainda, uma última abordagem que o atacante pode tentar que é um ataque por força bruta sobre as credenciais de aplicações *client* legítimas. Em outras palavras, o atacante pode tentar realizar requisições ao *token end-point* tentando "adivinhar" o valor do `client_secret`. 

Este tipo de ataque pode ser coibido através de várias técnicas (possivelmente utilizadas em conjunto). Uma delas é que o *authorization server* force a utilização de valores fortes para o `client_secret` (ou seja, segredos gerados de forma aleatória e com comprimentos suficientemente longos para tornar inviável um ataque por força bruta). Outra possibilidade é limitar o número de tentativas erradas consecutivas: depois de algumas falhas de autenticação do *client*, o servidor bloqueia o acesso deste durante algum tempo. Há, ainda, a hipótese de se utilizar algum método de autenticação mais forte, o que pode incluir o uso de certificados digitais por parte do *client*.

## Ataques *Cross-Site Request Forgery*

Como visto até aqui nesta aula, há várias possíveis maneiras pelas quais um atacante pode tentar quebrar a segurança do OAuth 2.0. No entanto, um ataque particularmente relevante é o *Cross-Site Request Forgery*, ou CSRF. 

Em aulas anteriores, já estudamos brevemente este ataque. Particularmente, discutimos este ataque no contexto dos autenticadores baseados em *cookies*. O ataque consiste em o atacante conseguir provocar que o *user-agent* da vítima gere uma requisição para um determinado serviço que requer autenticação. Se a vítima está autenticada naquele momento, a requisição é bem-sucedida, realizando a ação que o atacante desejava, e não algo que a vítima tencionava. Há várias formas pelas quais o atacante pode tentar forçar a vítima a realizar a requisição, incluindo embeber as URLs das requisições desejadas em elementos de uma página controlada pelo atacante, como imagens e *iframes*.

No contexto específico do OAuth 2.0, uma das possíveis variantes deste ataque funciona da seguinte maneira. O atacante começa por iniciar, ele próprio, um fluxo de autorização junto a algum *client* e algum *authorization server*. O atacante segue o fluxo normalmente, até a fase de redirecionamento para o *authorization end-point*. Neste momento, o atacante fornece as suas credenciais ao *authorization server* e aprova os pedidos de autorização aos seus recursos protegidos. Porém, quando o *authorization server* realiza o redirecionamento do *user-agent* do atacante de volta para o *client*, o atacante propositalmente interrompe o fluxo e não realiza a requisição. O atacante, então, regista a URL do redirecionamento e força o *resource owner* vítima a aceder a este endereço. Ao receber a requisição originada no *resource owner* vítima, o *client* procede à obtenção do *access token* utilizando o código recebido e associação a sessão da vítima a este *token*. A partir deste momento, qualquer ação da vítima na aplicação *client* será realizada com os recursos do atacante. Por exemplo, se a vítima tentar gravar informações através da aplicação *client*, estes serão gravados na conta do atacante e, portanto, serão facilmente acessíveis a este.

Para que o ataque e as suas consequências fiquem mais claros, vamos considerar um exemplo mais concreto. Suponha uma aplicação *web* hipotética de edição de fotografias denominada `www.photoedit.com`: o utilizador pode fazer o *upload* de ficheiros de imagem e realizar edições básicas. Após as edições, o utilizador tem a opção de realizar o *download* da imagem resultante ou, alternativamente, de gravá-la num serviço de armazenamento da nuvem. Digamos que um dos serviços de nuvem suportados seja o `www.mycloud.com`. Para que o `www.photoedit.com` seja capaz de gravar as fotografias do utilizador neste serviço, o `www.mycloud.com` fornece uma API cujos recursos são protegidos a partir de um serviço de autorização OAuth 2.0.

Digamos que a Alice, a vítima neste cenário, seja uma utilizadora frequente do `www.photoedit.com` e tenha por hábito justamente gravar suas fotos editadas justamente no `www.mycloud.com`.

O atacante, então, acede ao `www.photoedit.com` e solicita a esta aplicação *client* que obtenha autorização para gravar ficheiros **na conta do próprio atacante** no `www.mycloud.com`. Como consequência, o *client* `www.photoedit.com` inicia o fluxo *Authorization Code Grant*, redirecionando o *user-agent* do atacante para o *authorization end-point* do `www.mycloud.com`. O atacante, então, procede com a sua autenticação, utilizando as suas próprias credenciais no `www.mycloud.com`. Após a autenticação bem-sucedida, o *authorization server* do `www.mycloud.com` apresenta ao atacante a lista de permissões solicitadas pelo `www.photoedit.com`, as quais o atacante autoriza. 

Como consequência, o *authorization server* gera um *authorization code* associado aos recursos do atacante no `www.mycloud.com` e o envia em uma resposta de HTTP de redirecionamento de volta para o `www.photoedit.com` Porém, ao invés de aceitar o redirecionamento e realizar a requisição ao *end-point* do `www.photoedit.com`, o atacante interrompe o fluxo e grava a URL especificada pelo *authorization server*. 

Posteriormente, o atacante tenta forçar Alice a gerar uma requisição para aquela mesma URL. Isto pode ser feito de várias formas. Uma delas e através de *phishing*: o atacante pode enviar um e-mail para Alice contendo uma ligação para aquela URL, mas dizendo ser algum outro conteúdo do interesse da Alice. Outra alternativa é que o atacante construa algum *site* com algum conteúdo de interesse da Alice e numa das páginas inclua uma figura *iframe* ou qualquer outro elemento que faça referência àquela URL.

Seja qual for o método, o *user-agent* da Alice acaba por gerar uma requisição ao *end-point* do `www.photoedit.com` contendo o *authorization code* que foi gerado para o atacante. Quando o servidor de `www.photoedit.com` recebe esta requisição, ele usa o *Back Channel* para contactar o *token end-point* do *authorization server* do `www.mycloud.com`. Esta interação resulta na receção, por parte do `www.photoedit.com`, do *access token*. Mas lembre-se que este *access token* é associado ao *authorization code* que foi gerado pelo atacante e, portanto, dá acesso aos recursos do atacante, e não da Alice.

Mesmo assim, porque se trata de uma autorização bem-sucedida provocada por uma requisição da Alice, o servidor do `www.photoedit.com` associa o *access token* recebido à sessão da Alice. Isto significa que, a partir deste momento, outras interações da Alice com a aplicação `www.photoedit.com` resultarão em acessos aos recursos do atacante.

Além do problema mais óbvio -- Alice não terá acesso às fotos depois de editá-las --, a consequência principal é que o atacante ganhará acesso aos dados da Alice, já que ele poderá trivialmente aceder à sua própria conta em `www.mycloud.com` onde os dados agora residem.

Note que o mesmo tipo de ataque poderia ser realizado contra um serviço de autenticação baseado no OpenID Connect. Por exemplo, suponha agora um cenário ligeiramente diferente: `www.photoedit.com` utiliza um provedor de identidade chamado `www.idsprovider.com` para autenticar seus utilizadores, mas as fotos editadas são armazenadas no próprio servidor `www.photoedit.com` associadas à conta do utilizador. Neste cenário, o atacante possui uma conta em  `www.idsprovider.com` e inicia um fluxo de autenticação OpenID Connect para efetuar *login* no `www.photoedit.com`, interrompendo o fluxo logo que é redirecionado pelo *authorization server* de volta para o servidor do `www.photoedit.com`. O atacante, então, força Alice a aceder à URL especificada pelo *authorization server* no redirecionamento. Alice, então, fica com uma sessão ativa com `www.photoedit.com`, mas associada à conta do atacante. Por isto, todo o conteúdo gerado pela Alice na aplicação ficará registado, na verdade, na conta do atacante, que poderá aceder a este material posteriormente.

### Proteção Contra o CSRF no OAuth 2.0

Mas como podemos proteger um sistema baseado no OAuth 2.0 de um ataque do tipo CSRF? Mais geralmente, uma primeira linha de proteção contra ataques CSRF passa pela educação dos utilizadores a evitar seguir ligações nas quais não confiamos totalmente.

No entanto, algumas variantes do CSRF exploram técnicas que buscam forçar o *user-agent* a seguir tais ligações, mesmo sem uma intervenção explícita do utilizador (*e.g.*, através da inclusão das URLs de redirecionamento em elementos de páginas *web* especialmente construídas pelo atacante). Desta forma, é necessária a adoção de técnicas de proteção como parte do próprio fluxo de autorização que permitam aos vários participantes detetar tentativas de ataques.

Neste sentido, o principal mecanismo disponível no OAuth 2.0 para coibir ataques CSRF é o parâmetro `state`. Como vimos em aulas anteriores, o `state` é um parâmetro incluído na URL gerada pelo *client* no momento do redirecionamento do *user-agent* para o *authorization end-point*. Em outras palavras, no momento de solicitar este redirecionamento, o *client* **pode** criar um **valor de estado** associado àquele *resource owner* com quem está a comunicar-se e incluí-lo na URL que aponta para o *authorization end-point*. Por exemplo: `https://auth-server/authorize?response_type=code&client_id=s6BhdRkqt3&state=xyz`. Neste exemplo hipotético, o *client* gerou um estado com valor `xyz`. Além de incluir este valor na URL do redirecionamento, o *client* deve guardar a associação entre o *user-agent* do *resource owner* o valor do parâmetro `state` (por exemplo, este valor pode ser armazenado junto às demais informações de sessão do *user-agent*).

Quando o *authorization server* recebe uma requisição ao seu *authorization end-point* contendo o parâmetro `state` ele é obrigado a incluir exatamente o mesmo parâmetro na URL a ser utilizada para o redirecionamento do *user-agent* de volta ao *client*. Em continuidade ao exemplo anterior, após a autenticação e autorização bem-sucedidas do *resource owner*, o *authorization server* pode especificar um redirecionamento para a seguinte URL: `https://client.example.com/cb?code=SplxlOBeZQQYbYS6WxSbIA&state=xyz`. 

Por sua vez, quando o *client* recebe uma requisição à URL da sua *callback* contendo o parâmetro `state`, ele pode extrair o seu valor e compará-lo ao valor de estado associado à sessão atual do *user-agent* que realiza a requisição. Se os valores de estado forem iguais, assume-se que este *user-agent* é o mesmo que realizou a requisição original para iniciar o processo de autorização. Caso contrário, assume-se que se trata de um ataque CSRF e a requisição não é aceite.

Para que esta contramedida funcione, é importante que o valor escolhido para o parâmetro não seja previsível para ou forjável pelo atacante. Uma abordagem comum é que o *client* atribua como valor do parâmetro *state* um *hash* do identificador de sessão atribuído para aquele *user-agent*. Esta abordagem tem a vantagem adicional de não requerer que o *client* armazene o mapeamento entre o valor do parâmetro *state* e o *user-agent*: basta que na requisição à *callback* do *client* este obtenha o identificador de sessão a partir do *cookie* de sessão incluído pelo *user-agent*, calcule o seu *hash* e, de seguida, compare o resultado ao valor do parâmetro `state` recebido na URL.




