# Aula 13 - Autenticação Baseada em *Passwords*

A partir desta aula, daremos início à segunda parte desta UC. Aqui, deixaremos de focar explicitamente em criptografia (métodos, esquemas e protocolos) e abordaremos aspetos de segurança informática de mais alto nível. Tais aspetos continuam a depender de / utilizar técnicas criptográficas, mas, em grande parte, abstrairemos a criptografia e focaremos em outras partes do sistema.

Nesta aula, em particular, discutiremos o problema da **autenticação baseada em *passwords***. Ou seja, em como podemos utilizar *passwords* para realizar a autenticação da identidade de um utilizador. Começaremos por definir e distinguir os conceitos de **autenticação** e **identificação**. Depois, discutiremos os vários tipos de informação que podem ser usados para autenticação de utilizadores. Falaremos especificamente sobre *passwords* e seus riscos. Em seguida, definiremos como funciona um sistema de autenticação baseado em *passwords* e discutiremos os possíveis ataques a ele. Por fim, falaremos sobre contra-medidas a estes ataques utilizadas na prática.

## Identificação e Autenticação

Em segurança informática, identificação e autenticação são dois conceitos relacionados, porém distintos. A **identificação** diz respeito à definição de uma informação que estabelece quem é um utilizador ou entidade. Exemplos de identificação incluem endereços de e-mail, *usernames* e números de telefone. Já a autenticação refere-se ao processo de verificação da identidade alegada por este utilizador ou entidade. Ou seja, é um processo através do qual o utilizador ou entidade **comprova** a sua identificação.

Para diversos sistemas informáticos, conhecer a identificação verdadeira de uma entidade ou utilizador é fundamental. Por exemplo, é com base nesta identificação que posteriormente uma aplicação pode aplicar políticas de controlo de acessos, de forma a restringir os acessos aos seus recursos àqueles utilizadores autorizados. Sistemas informáticos também utilizam a identificação para apresentar comportamentos personalizados para cada utilizador (*e.g.*, temas e cores personalizados, informações sobre a conta do utilizador). A identificação também é importante para propósitos de auditoria, à medida que o sistema pode registar num *log* as ações de um utilizador associadas à sua identidade.

Há várias formas através das quais um sistema informático pode realizar a identificação e autenticação de um utilizador. Uma das formas mais comuns - exatamente o objeto desta aula - é através de um par *user* + *password*. Ou seja, o sistema requisita que o utilizador forneça um *username* (uma identificação) e uma *password* (uma prova de autenticidade da identificação).

## Tipos de Informação de Autenticação

Embora *passwords* sejam uma forma bastante tradicional de autenticação, elas não são a única alternativa. Na verdade, existem vários **fatores de autenticação**. No contexto da segurança informática, os fatores de autenticação dizem respeito a tipos de elementos que podem ser utilizados para autenticar a identidade de um utilizador. Os principais fatores de autenticação são:

- **Conhecimento**: diz respeito a elementos que supostamente só podem ser conhecidos pelo utilizador legítimo. Isto inclui *passwords* e *passphrases* (*passphrases* são *strings* similares a *passwords*, mas substancialmente mais longas e tipicamente com um significado mais claro para o utilizador).
- **Propriedade**: diz respeito a elementos que supostamente apenas o utilizador legítimo possui. Exemplos incluem *tokens* criptográficos ou acesso a um telemóvel para receber mensagens de texto.
- **Característica**: diz respeito a características biométricas do utilizador, tal como impressões digitais ou a sua iris.

Em certos contextos, pode-se considerar como um outro possível fator o "algo que se faz". Ou seja, alguma ação passível de verificação que, supõe-se, seja exequível apenas pelo utilizador legítimo. Um exemplo tradicional disto é a assinatura manual de um documento (sob a hipótese de que uma terceira parte não seria capaz de reproduzir a assinatura).

Sistemas informáticos modernos frequentemente combinam vários destes fatores para implementar o que se convenciona chamar de uma **autenticação de múltiplos fatores** - também chamada de **autenticação de dois fatores** ou **autenticação de três fatores**, a depender do número de fatores usados. Por exemplo, o sistema pode inicialmente solicitar um par *username* + *password* e, caso a verificação seja bem-sucedida, enviar um e-mail com um código de verificação para que o utilizador comprove acesso (*i.e.*, propriedade) àquela conta de e-mail. O uso destes vários fatores em conjunto reduz a probabilidade de que um atacante consiga forjar uma autenticação, dado que se torna necessário que ele não só conheça a *password*, mas também tenha acesso a outros elementos.

## Autenticação e Controlo de Acessos

Em sistemas informáticos, a autenticação é frequentemente utilizada em conjunto com um serviço de autorização de forma que o sistema possa realizar um controlo de acessos adequado aos seus recursos (*i.e.*, para cada recurso disponibilizado pelo sistema, permitir acesso apenas pelos utilizadores autorizados).

Numa arquitetura típica, o utilizador primeiramente interage com o serviço de autenticação fornecendo a este suas credenciais (*e.g.*, nome de utilizador e *password*). O serviço de autenticação, então, valida estas credenciais. Tal validação pode ser realizada consultando-se **uma base de dados local** ou às custas de um **serviço de autenticação externo**. Esta última alternativa tem se tornado cada vez mais comum, uma vez que empresas como Google, Apple a Microsoft, que contam com bases de utilizadores bastante significativas, proveem serviços deste género. Em aulas futuras, estudaremos esta abordagem em mais detalhes.

Uma vez autenticado, as requisições do utilizador passam pelo serviço de autorização. Este serviço, que também será estudado em maiores detalhes posteriormente nesta disciplina, consulta uma base de dados de políticas de controlo de acesso e verifica se aquele utilizador em particular tem permissão para aceder ao recurso desejado. Em caso afirmativo, a requisição é encaminhada à porção do sistema responsável pela implementação dos serviços.

Ignorando por ora o serviço de autorização e tendo foco apenas na autenticação, esta arquitetura apresenta três potenciais pontos de ataque. O primeiro é a própria **interface de autenticação**. Um atacante pode, por exemplo, tentar realizar um ataque por força bruta. Para isto, ele utiliza a própria interface de autenticação e realiza sucessivas tentativas de autenticação percorrendo várias possíveis credenciais.

O segundo potencial ponto de ataque é o serviço externo de autenticação, **assumindo-se que um é utilizado**. Neste caso, o sistema informático em questão irá confiar plenamente nas informações providas por este serviço externo. Caso o serviço externo, por qualquer motivo, seja comprometido e passe a fornecer informações incorretas (*e.g.*, atestar a autenticidade de um utilizador quando, na verdade, trata-se de um atacante), o sistema informático confiará nesta informação falsa.

Por fim, o terceiro potencial ponto de ataque é a base local de validação das credenciais dos utilizadores. Caso um atacante tenha acesso a esta base, ele pode realizar várias ações maliciosas que comprometerão o serviço de autenticação. Isto inclui adicionar utilizadores falsos ou, eventualmente, recuperar as credenciais corretas de utilizadores legítimos.

## Vulnerabilidades de *Passwords* Textuais

Embora *passwords* sejam ainda amplamente utilizadas como parte das credenciais de autenticação em diversos sistemas informáticos, elas são frequentemente suscetíveis a ataques. Particularmente, *passwords* muitas vezes são vulneráveis aos chamados **ataques de dicionário**.

Um ataque de dicionário é muito similar a um ataque por força bruta: o atacante testa possíveis combinações de *passwords* para um ou mais utilizadores legítimos, em busca de uma que seja corretamente validada. Ocorre que nos ataques de dicionário, as *passwords* testadas pelo atacante não correspondem a todas as combinações possíveis. Ao contrário, os atacantes exploram o facto de que comumente utilizadores escolhem *passwords* que correspondem a palavras em uma determinada lingua ou pequenas variações disto (*e.g.*, palavras concatenadas com números que representam anos). Existem, inclusive, diversos estudos e bases de *passwords* comumente utilizadas. Com base neste conhecimento, é possível construir um **dicionário**, ou seja, uma grande lista de *passwords* comuns. O ataque de dicionário, portanto, consiste na tentativa, por parte do atacante, de validar cada uma das *passwords* no dicionário.

Um ataque de dicionário pode ser realizado tanto contra a interface de autenticação do sistema (*e.g.*, o atacante escreve um programa que percorre o dicionário e tenta realizar a autenticação utilizando cada possível *password* na própria interface de autenticação do sistema) quanto diretamente contra a base onde estão guardadas as informações de validação. Mais a frente, veremos mais detalhes de como isto é feito neste último caso.

## Sistema de Autenticação

Antes de prosseguirmos com um estudo mais aprofundado das técnicas envolvidas no processo de autenticação, é importante definirmos de forma genérica como um sistema de autenticação funciona e alguma nomenclatura associada.

Vimos anteriormente que um sistema de autenticação possui uma interface através da qual o utilizador envia suas credenciais, além de uma base local que serve para validá-las. Numa primeira tentativa de projetar tal sistema, poderíamos supor que a base local contém entradas para cada utilizador registado e, por sua vez, cada entrada armazena, por exemplo, a *password* do respetivo utilizador. 

Esta, no entanto, não é uma boa alternativa por uma série de motivos. Em primeiro lugar, um administrador do sistema teria acesso a esta base e, portanto, poderia descobrir as *passwords* de todos os utilizadores. Repare, ainda, que utilizadores tendem a usar a mesma *password* para vários sistemas diferentes, o que agrava as consequências deste acesso do administrador a esta informação. Similarmente, caso esta base seja comprometida e um atacante ganhe acesso a ela, ele poderia também facilmente descobrir as *passwords* de todos os utilizadores.

Por estes motivos, os sistemas de autenticação tendem a não armazenar diretamente a *password* dos utilizadores. Ao invés disto, a base local armazena apenas o que chamamos de **informação de validação**. A informação de validação é alguma informação que pode ser facilmente calculada a partir da *password*/credenciais do utilizador, mas a partir da qual deve ser difícil obter de volta a *password*/credenciais. Genericamente, chamaremos de $f(.)$ a função que transforma a *password* (ou, mais geralmente, a **informação de autenticação** do utilizador) na informação de validação . Uma escolha popular para a função $f(.)$ é alguma função de *hash* criptográfico, dado que elas são simples de se calcular, porém não são inversíveis.

Além da função $f(.)$, o sistema de autenticação precisa ainda de uma função auxiliar $g(v)(a)$. O propósito da função $g(.)$ é validar que a informação de autenticação calculada a partir de $a$ corresponde ao que se encontra armazenado na base local ($v$). Por exemplo, podemos definir $g(v)(a) = (v = f(a))$. Note que o retorno da função $g(.)$ é um valor booleano: *true* significa que a validação foi bem sucedida (*i.e.*, o utilizador foi autenticado), enquanto *false* significa falha na validação.

Para que estes conceitos fiquem mais claros, podemos tomar como exemplo o sistema de autenticação utilizado no Linux. A base local de informações de validação corresponde a um par de ficheiros denominados `/etc/passwd` e `/etc/shadow`. A autenticação é baseada em *passwords*, pelo que a informação de autenticação $a$ é uma palavra-passe. Para cada utilizador o `/etc/shadow` armazena um *hash* criptográfico da *password*, o que constitui a informação de validação $v$. Portanto, $v = f(a) = H(a)$ para alguma função de *hash* criptográfico $H(.)$ (na verdade, estamos aqui omitindo alguns detalhes deste processo; mais à frente, veremos estes detalhes adicionais).

Quando um utilizador tenta autenticar-se no sistema, a interface de autenticação solicita as credenciais (nome de utilizador e *password*). O sistema, então, utiliza o nome de utilizador fornecido para buscar nos ficheiros a informação de validação $v$. Posteriormente, o sistema calcula a função $g(v)(a)$: computa-se $H(a)$ e compara-se este valor com o $v$ encontrado nos ficheiros. Se os valores forem iguais, assume-se que a *password* fornecida é correta e a autenticação é bem-sucedida. Caso contrário, a *password* é incorreta e o sistema não autentica o utilizador.

> [!NOTE]
> 
> Ilustração do conteúdo típico dos ficheiros `/etc/passwd` e `/etc/shadow`. 
>
> - Num sistema Linux, assumindo um utilizador com permissões suficientes, podemos visualizar o conteúdo destes dois ficheiros. Por exemplo, para um sistema hipotético, os conteúdos poderiam ser:
> ```bash
> $ cat /etc/passwd
> root:x:0:0:root:/root:/bin/bash
> daemon:x:1:1:daemon:/usr/sbin:/usr/sbin/nologin
> bin:x:2:2:bin:/bin:/usr/sbin/nologin
> sys:x:3:3:sys:/dev:/usr/sbin/nologin
> sync:x:4:65534:sync:/bin:/bin/sync
> games:x:5:60:games:/usr/games:/usr/sbin/nologin
> man:x:6:12:man:/var/cache/man:/usr/sbin/nologin
> lp:x:7:7:lp:/var/spool/lpd:/usr/sbin/nologin
> mail:x:8:8:mail:/var/mail:/usr/sbin/nologin
> news:x:9:9:news:/var/spool/news:/usr/sbin/nologin
> uucp:x:10:10:uucp:/var/spool/uucp:/usr/sbin/nologin
> proxy:x:13:13:proxy:/bin:/usr/sbin/nologin
> www-data:x:33:33:www-data:/var/www:/usr/sbin/nologin
> backup:x:34:34:backup:/var/backups:/usr/sbin/nologin
> list:x:38:38:Mailing List Manager:/var/list:/usr/sbin/nologin
> irc:x:39:39:ircd:/run/ircd:/usr/sbin/nologin
> gnats:x:41:41:Gnats Bug-Reporting System (admin):/var/lib/gnats:/usr/sbin/nologin
> nobody:x:65534:65534:nobody:/nonexistent:/usr/sbin/nologin
> _apt:x:100:65534::/nonexistent:/usr/sbin/nologin
> alice:x:1000:1000::/home/alice:/bin/sh
> bob:x:1001:1001::/home/bob:/bin/sh
> $ cat /etc/shadow
> root:*:19634:0:99999:7:::
> daemon:*:19634:0:99999:7:::
> bin:*:19634:0:99999:7:::
> sys:*:19634:0:99999:7:::
> sync:*:19634:0:99999:7:::
> games:*:19634:0:99999:7:::
> man:*:19634:0:99999:7:::
> lp:*:19634:0:99999:7:::
> mail:*:19634:0:99999:7:::
> news:*:19634:0:99999:7:::
> uucp:*:19634:0:99999:7:::
> proxy:*:19634:0:99999:7:::
> www-data:*:19634:0:99999:7:::
> backup:*:19634:0:99999:7:::
> list:*:19634:0:99999:7:::
> irc:*:19634:0:99999:7:::
> gnats:*:19634:0:99999:7:::
> nobody:*:19634:0:99999:7:::
> _apt:*:19634:0:99999:7:::
> alice:$y$j9T$KFXzGy0RXYho3ReDdB0P7.$YbRfWjHHnQRIId3Nt6QlxLyeJC1iwGDafsRuo0YrAu/:19667:0:99999:7:::
> bob:$y$j9T$pBMdTYpcnHMWL2nftP.T90$Cv5Z/xp3A6J4IxmUWkWttMlE6ZlIEQX/xFvwhChy.q5:19667:0:99999:7:::
> ``` 
>
> - Observe que o ficheiro `/etc/passwd`, apesar do nome, não armazena nem informações de autenticação nem informações de validação dos utilizadores. Ao contrário, este ficheiro guarda apenas informações como a diretória *home* do utilizador e outras preferências.
> - Por outro lado, o ficheiro `/etc/shadow` efetivamente contém as informações de validação. Cada linha corresponde a um utilizador e os respetivos campos de informação são separados por dois-pontos. O segundo campo é justamente a informação de validação, que corresponde a um valor de *hash* computado sobre a *password* do utilizador. 
> - Observe que a informação de validação presente no `/etc/shadow` é mais que apenas o valor do *hash*. Na verdade, há várias informações ali representadas. Veremos quais estas são mais à frente nesta aula.


## Ataques de Dicionário

Voltemos agora ao ataque de dicionário. Discutimos anteriormente que este tipo de ataque pode ser executado de duas formas diferentes: ou através da própria interface de autenticação do sistema ou diretamente sobre a base local de informações de validação, caso o atacante tenha acesso a esta.

Ataques de dicionário executados sobre a interface de autenticação do sistema constituem os chamados **ataques do tipo 2**. Ataques do tipo 2 caracterizam-se pelo atacante não ter acesso à informação de validação $v$ relativa ao utilizador legítimo armazenada na base local do sistema. Neste caso, o atacante tem apenas a capacidade de executar a função $g(.)$ sucessivamente fornecendo como entradas várias tentativas de valores diferentes para a informação de autenticação $a$. Lembre-se que $a$ corresponde às credenciais fornecidas ao sistema, de forma que o atacante pode simplesmente percorrer seu dicionário e submeter cada *password* lá contida ao sistema, verificando se o resultado da autenticação é positivo.

Por outro lado, nos **ataques do tipo 1**, o atacante tem acesso direto à informação de validação $v$. Isto pode ocorrer, por exemplo, se o atacante teve acesso à base local de validação do sistema. Nesta hipótese, o ataque de dicionário pode ser realizado de maneira mais eficiente porque, para cada *password* $a$ do dicionário, basta que o atacante calcule $f(a)$ e verifique se o valor obtido é igual a $v$. Isto torna a avaliação de cada entrada do dicionário mais rápida e, portanto, aumenta a probabilidade de sucesso do ataque como um todo.

> [!NOTE]
> 
> Ilustração de um ataque de dicionário do tipo 1.
>
> - Considere que um atacante teve acesso ao ficheiro `/etc/shadow` hipotético mostrado acima. Existem várias ferramentas especializadas que permitem conduzir um ataque de dicionário do tipo 1, dada uma base de informações de validação como esta. Um exemplo é utilitário `john` (também conhecido como *John the Ripper*).
> - Embora este programa tenha várias opções para controlarmos a sua forma de operação, um exemplo de execução seria:
>
> ```bash
> $ john --format=crypt /etc/shadow
> Created directory: /root/.john
> Loaded 2 password hashes with 2 different salts (crypt, generic crypt(3) [?/64])
> Will run 12 OpenMP threads
> Press 'q' or Ctrl-C to abort, almost any other key for status
> qwerty           (bob)
> 123456           (alice)
> 2g 0:00:00:29 100% 2/3 0.06865g/s 204.1p/s 207.4c/s 207.4C/s 123456..pepper
> Use the "--show" option to display all of the cracked passwords reliably
> Session completed
> ```
>
> - O programa começa por ler o ficheiro com a base de informações de validação -- neste caso, o `/etc/shadow` -- e carregar todos os *hashes* e *salts* encontrados.
> - De seguida, o programa começa o processo de tentativas sucessivas de *passwords*: para cada *password* gerada ou presente no dicionário, computa-se o *hash* utilizando o *salt* de cada utilizador. 
> - Finalmente, o resultado é comparado ao *hash* carregado a partir do ficheiro. Caso haja um casamento, a *password* encontrada é mostrada.
> - Neste exemplo, ambas as *passwords* dos utilizadores `bob` e `alice` foram quebradas com sucesso.

## Proteção Contra Ataques de Dicionário

Neste ponto, já discutimos em bastante detalhe os ataques de dicionário da perspectiva do atacante. No entanto, é importante consideramos a perspectiva oposta: como é possível proteger um sistema informático contra ataques deste tipo? Felizmente, há várias contramedidas que podem ser adotadas, sejam individualmente ou em conjunto. 

A primeira possibilidade é reduzir a probabilidade de que as *passwords* dos utilizadores do sistema constem no dicionário. Lembre-se que os dicionários são construídos com base em *passwords* simples e populares. Se, de alguma forma, garantirmos que os **utilizadores usam *passwords* mais complexas**, reduzimos a probabilidade de que o dicionário as contenha e, com isto, as tentativas de ataque não serão bem-sucedidas.

Aqui, o adjetivo *complexa* denota uma série de características da *password*. Em primeiro lugar, a *password* não deve ser baseada em uma palavra ou algo similarmente previsível. Além disto, idealmente, *passwords* devem ser relativamente longas: quanto mais longa uma *password*, menor a probabilidade de que ela seja encontrada num dicionário, dado que o número de possíveis *passwords* com $n$ carácteres aumenta exponencialmente com o aumento de $n$. Igualmente, *passwords* que misturam letras, números e carácteres especiais são mais seguras, particularmente por aumentarmos o número de possíveis combinações.

Vários sistemas incluem regras específicas que definem características mínimas que devem ser atendidas pelas *passwords* dos utilizadores. Quando um utilizador é registado no sistema, a *password* escolhida é validada contra estas regras e só é aceita se atendê-las. Outros sistemas simplesmente geram *passwords* aleatórias de acordo com parâmetros de complexidade pré-estabelecidos (*e.g.*, número de carácteres, tipos de carácteres usados) e as informam aos utilizadores durante o processo de registo.

> [!NOTE]
> 
> Ilustração de geração de *passwords* aleatórias através de serviços como o `random.org` e aplicações similares.
>
> - Aceder ao https://random.org/.
> - No menu, selecionar `Lists & More > Passwords`. 
> - Preencher as informações solicitadas e gerar as passwords.
> - Note que, claramente, o site conhece as *passwords* geradas, pelo que podemos não querer utilizá-las para propósitos particularmente sensíveis.
> - Outra alternativa são programas específicos para a geração de *passwords*, como o `pwgen`:
>
> ```bash
> $ pwgen 20 3 
> ```
>   - O comando solicita a geração de 3 *passwords* de 20 carácteres cada.

É importante referir que estas estratégias para o aumento da complexidade ou da incerteza das *passwords* também podem resultar no efeito oposto, caso sejam adotadas medidas em excesso. Lembre-se que uma *password* corresponde a um fator de autenticação do tipo *conhecimento*: ou seja, assume-se que é algo que o utilizador conhece. Porém, caso um sistema exija ou gere uma *password* excessivamente complexa, o utilizador terá dificuldade em decorá-la e poderá mantê-la anotada, transformando-a num fator de autenticação de propriedade, e introduzindo a possibilidade de que a mesma seja extraviada. 

Outra contramedida de proteção contra ataques de dicionário, particularmente os do tipo 1, é controlar o acesso à informação de verificação. Dado que os ataques do tipo 1 são potencialmente mais efetivos, é importante evitar que atacantes tenham acesso a informação de verificação. Isto significa restringir a um mínimo as entidades que têm acesso a base local de informação de verificação.

Outra medida que ajuda a coibir os ataques de dicionário é o **aumento do tempo de processamento da função $f$**. Como vimos, esta função é frequentemente apenas um *hash* criptográfico $H(.)$. Assim, uma alternativa é a utilização de funções de *hash* com maior processamento. Outra alternativa é definir a função $f(.)$ como $f(a) = H^R(a) = H(H(...(H(a))))$. Ou seja, a função de *hash* seria aplicada inicialmente à informação de autenticação $a$. De seguida, o resultado desta primeira aplicação seria passado como entrada para uma nova execução de $H(.)$. Este processo seria repetido $R$ vezes.

No caso particular dos ataques do tipo 2, uma outra possibilidade é aumentar o tempo de processamento ou limitar o acesso à função $g(v)$. Mais concretamente, alguns sistemas adicionam um atraso artificial na sua resposta sempre que o processo de validação falha. Ou seja, caso as credenciais fornecidas sejam inválidas, ao invés de o sistema reportar esta falha o mais rápido possível, ele introduz um atraso artificial. Para um utilizador legítimo que simplesmente cometeu um erro ao digitar sua *password*, um atraso adicional de 1 ou 2 segundos é pouco relevante. Mas para um atacante que tem um dicionário com milhões de *passwords* a serem testadas, este tempo adicional pode efetivamente inviabilizar o ataque.

Outra abordagem similar é o estabelecimento de uma política de bloqueio ou limitação depois de um determinado número de falhas consecutivas de autenticação. A ideia é que é pouco provável que um utilizador legítimo erre repetidas vezes ao fornecer suas credenciais. Num ataque de dicionário, por outro lado, espera-se uma grande quantidade de erros consecutivos.

Existem várias técnicas diferentes para realizar este bloqueio ou limitação. Uma delas é o chamado ***backoff***, que consiste na adição de um **tempo artificial** para a resposta da função $g(.)$ **que cresce a cada nova tentativa errada**. Isto faz com que o sistema não seja inteiramente bloqueado, porém tende a dificultar o processo de tentativa e erro utilizado nos ataques de dicionário.

Outra possibilidade é a **terminação da ligação**. Isto aplica-se, particularmente, a aplicações em redes (*e.g.*, uma aplicação *web*). Após um determinado número de tentativas incorretas, o servidor simplesmente termina a ligação com o cliente, obrigando-o a estabelecer uma nova ligação para realizar novas tentativas de autenticação. Novamente, a ideia é tornar mais lento o processo de tentativa e erro do ataque.

Uma alternativa mais extrema é o **bloqueamento**. Neste caso, o sistema não só termina a ligação atual após um determinado número de falhas, como também bloqueia novas tentativas de acesso originadas daquela entidade (seja permanentemente, seja por um período de quarentena). Esta alternativa do bloqueamento é particularmente efetiva contra atacantes, mas, por outro lado, pode causar problemas de disponibilidade do sistema para os utilizadores legítimos.

Por fim, uma alternativa usada por certos sistemas é o chamado ***Jailing***. Neste caso, o sistema não efetua nenhum tipo de bloqueamento em caso de erros sucessivos. No entanto, o sistema assinala aquela tentativa de acesso como suspeita e, caso posteriormente a autenticação seja bem-sucedida, o sistema entra num modo de operação com funcionalidade limitada de forma a mitigar o potencial de danos que podem ser causados caso o acesso tenha sido feito por um atacante.

Seja qual for a alternativa adotada, é necessário balancear o risco de o sistema sofrer um ataque de dicionário bem-sucedido com o risco de tornar o sistema indisponível para um utilizador legítimo.

Outra estratégia é aumentar o custo de realizar uma tentativa de autenticação de maneira a desincentivar atacantes a tentar atacar o sistema. Isto pode ser feito através de "desafios": o sistema solicita que a entidade que tenta realizar a autenticação resolva um determinado desafio computacional antes de permitir uma tentativa de autenticação. Estes desafios em geral são problemas que demandam algum esforço computacional para serem resolvidos, de forma que o esforço necessário pode ser considerado excessivo por um atacante (que tipicamente terá que realizar um elevado número de tentativas). Este esforço, por outro lado, pode ser considerado pequeno por um utilizador legítimo, já que este provavelmente necessitará de apenas uma tentativa.

Similarmente, é comum em sistemas modernos o uso de **Testes de Turing**. Um **Teste de Turing** é um conceito introduzido por Alan Turing que consiste num teste com o objetivo de identificar se um determinado interlocutor é realmente um humano - ou apenas um computador a ser passar por um humano. Solicitar que a entidade que tenta realizar a autenticação passe por um Teste de Turing evita a automatização dos ataque de dicionário. Em geral, devido ao grande número de entradas no dicionário, um atacante irá automatizar o ataque através de um programa ou script. A existência de um Teste de Turing impede esta automatização e, portanto, dificulta a execução do ataque. Um exemplo de tecnologia muito utilizada para Testes de Turing neste contexto é o CAPTCHA.

## Ataques com Pré-Computação

Uma variante particularmente problemática dos ataques de dicionário do tipo 1 são os ataques com pré-computação. A ideia básica é a mesma: existe um dicionário de *passwords* comuns ou prováveis, e o atacante adicionalmente tem acesso à informação de validação $v$. No entanto, ao invés de proceder como num ataque de dicionário tradicional e calcular no momento do ataque a função $f(.)$ para cada entrada do dicionário, o atacante realiza previamente uma pré-computação destes valores.

Durante a pré-computação, o atacante cria um ***array* associativo** M. Para cada entrada $a_i$ do dicionário, o atacante armazena o par $(f(a_i), a_i)$ no *array*. Em outras palavras, $M[f(a_i)] = a_i$. Coloquialmente, os *arrays* resultantes deste pré-processamento são conhecidos como *Rainbow Tables*.

Ao final deste pré-processamento, torna-se **trivial executar o ataque**: dada a informação de autenticação $v$, basta retornar $M[v]$, se tal entrada existir no *array*. 

Embora o pré-processamento demande um tempo substancial por parte do atacante, o *array* M resultante pode ser reutilizado várias vezes. Assim, este método é particularmente eficaz quando o atacante possui as informações de validação de vários utilizadores e deseja tentar obter a *password* de ao menos um deles. 

## *Salt*

No contexto da Segurança Informática, *salt* é um termo que denota um **mecanismo simples de proteção contra ataques de pré-computação**. Concretamente, um *salt* é um número - ou sequência de bytes - aleatório que é adicionado ao processo de cálculo da função $f(.)$.

Mais especificamente, quando o sistema realiza o registo de um utilizador $u$, ele **sorteia aleatoriamente** um $salt_u$ específico para aquele utilizador. A informação de validação do utilizador $u$, $v_u$, é então calculada como $v_u = f(salt_u || a_u)$. De seguida, **ambos o $salt_u$ e o $v_u$ são armazenados na base local de informação de validação do sistema.**

Analogamente, quando um utilizador tenta realizar a autenticação e fornece uma informação de autenticação $a$, o sistema busca o $salt$ daquele utilizador e usa o resultado de $f(salt || a)$ para comparação com a informação de validação $v$.

Mas como o uso do *salt* coíbe os ataques de pré-computação? Lembre-se que o benefício da pré-computação é o facto de que o mesmo array $M$ pode ser utilizado para atacar vários/todos os utilizadores do sistema. No entanto, dado que **utilizadores diferentes terão provavelmente valores diferentes de *salt***, não é possível realizar uma única pré-computação que sirva para atacar todos os utilizadores. Hipoteticamente, o atacante poderia alterar o procedimento de pré-computação de forma a criar entradas no *array* para todos as possíveis combinações de *passwords* no dicionário e valores possíveis de *salt*. Isto, no entanto, rapidamente se **torna inviável**, por conta do grande número de possíveis valores de *salt*.

> [!NOTE]
> 
> Ilustração do uso de *salt* na autenticação de utilizadores no Linux. 
>
> - Lembre-se do conteúdo do ficheiro `/etc/shadow` hipotético mostrado anteriormente (particularmente, suas entradas para os utilizadores `alice` e `bob`):
>
> ```bash
> # cat /etc/shadow
> ...
> alice:$y$j9T$KFXzGy0RXYho3ReDdB0P7.$YbRfWjHHnQRIId3Nt6QlxLyeJC1iwGDafsRuo0YrAu/:19667:0:99999:7:::
> bob:$y$j9T$pBMdTYpcnHMWL2nftP.T90$Cv5Z/xp3A6J4IxmUWkWttMlE6ZlIEQX/xFvwhChy.q5:19667:0:99999:7:::
> ``` 
>
> - A informação de validação associada à utilizadora `alice` é `$y$j9T$KFXzGy0RXYho3ReDdB0P7.$YbRfWjHHnQRIId3Nt6QlxLyeJC1iwGDafsRuo0YrAu/`. Esta informação é, na verdade, decomposta em várias componentes separadas pelo carácter `$`:
>   - Tipo do *hash*: `y` (denota uma função de *hash* chamada *yescrypt*).
>   - Parâmetros adicionais à função de *hash*: `j9T`.
>   - Valor do *salt*: `KFXzGy0RXYho3ReDdB0P7.`
>   - Valor do *hash* resultante: `YbRfWjHHnQRIId3Nt6QlxLyeJC1iwGDafsRuo0YrAu/`.
>   - Observe que as componentes são geralmente codificadas em Base64.

## O Sistema *Bcrypt*

Embora a forma mais comum de armazenamento das informações de validação seja na forma de um *hash* criptográfico da informação de autenticação -- possivelmente acrescido de um *salt* --, nem toda função da *hash* criptográfico é igualmente boa para este propósito. 

Particularmente, funções como a SHA-256, amplamente consideradas seguras para diversos propósitos criptográficos, têm uma característica em particular que as torna menos interessante para esta aplicação de armazenamento de *passwords*: elas são **demasiado rápidas**. Lembre-se da aula sobre funções de *hash* criptográfico que rapidez de processamento era uma característica desejável para estas funções, porque gostaríamos de computá-las rapidamente para fins legítimos. Por exemplo, numa ligação TLS que envia centenas de registos por segundo, precisamos computar códigos MAC para cada registo o que inclui várias aplicações de uma função da *hash*. Se apenas a computação da função de *hash* leva, digamos, 1 segundo, então o *overhead* deste processamento começará a interferir de forma significativa no desempenho da comunicação.

No caso do armazenamento de *passwords*, no entanto, o processo de verificação da informação de validação não é tão sensível ao tempo. Em outras palavras, se a resposta do sistema de autenticação demora, digamos, 1 segundo, isto não afeta significativamente seu funcionamento. Com isto, funções de *hash* mais pesadas computacionalmente podem ser empregadas.

Mais que isto, **funções de *hash* mais custosas são desejáveis** neste cenário. Isto porque, no caso de que um atacante consiga acesso às informações de validação, queremos **dificultar ao máximo o ataque de dicionário**. Logo, se aumentarmos significativamente o esforço computacional para o cálculo do *hash* de cada *password* testada pelo atacante, podemos eventualmente tornar o ataque inviável.

Embora, historicamente, funções de *hash* como o MD5 e o SHA-256 tenham sido utilizadas para o armazenamento de *passwords* em sistemas reais, pelos motivos expostos acima, hoje **existem várias funções especializadas** para este fim. 

Uma destas é a bcrypt. Além de apresentar uma **computação mais intensiva**, a bcrypt tem também um nível de **dificuldade parametrizável**. Este nível de dificuldade é controlado por um parâmetro denominado *custo*. Na prática, este custo representa a quantidade de iterações realizadas pelo algoritmo, que pode ser aumentada para tornar o processamento mais lento.

A bcrypt é utilizada por uma série de sistemas práticos e, por isto, conta com várias implementações em diferentes linguagens. Por exemplo, em `node.js` há o pacote `bcrypt` que dá acesso a métodos de geração do *hash* -- *e.g.*, para quando queremos registar um novo utilizador no sistema ou alterar sua *password* -- e para a verificação do mesmo -- *e.g.*, quando queremos autenticar o utilizador.

Um exemplo de utilização do pacote `bcrypt` para geração do *hash* correspondente a uma *password* pode ser visto abaixo:

```node
const bcrypt = require('bcrypt');​

// Palavra-passe a proteger​
const password = 'changeit';​
const saltRounds = 12; // número de iterações​

bcrypt.hash(password, saltRounds, (err, hash) => {​

  if (err) {​
    return console.error('Erro no cálculo:', err);​
  }​
  console.log('Hash gerado:', hash);​
});
```

O método principal aqui é o `hash()`. Ele recebe como parâmetros a *string* que contém a *password* sobre a qual queremos computar o *hash*, o parâmetro de custo (*i.e.*, quantas iterações queremos) e uma *callback* chamada quando o resultado está disponível. Considere agora uma sequência de três execuções do código acima e seus respetivos resultados:

```bash
$ node bcrypt.js 
Hash gerado: $2b$12$JfdMbEiXAVoJ6cM9wuOegOeqxkoN0nd6BcpjN9O.0IdLSOzBpqc2q
$ node bcrypt.js 
Hash gerado: $2b$12$q.vzidaVxmQbmdx87PH3N.zDdE8RuV8U2e0IE3YqoeyK74wSIxECe
$ node bcrypt.js 
Hash gerado: $2b$12$XxXRA.muz5BuOVlD2jYZDuFbnK2ALDpCDfshL.Mwa8GqS5WzajKoa
```

Note que o formato da saída é similar ao das informações de validação vistas nos exemplos do ficheiro `/etc/shadow` mostrados anteriormente, no sentido de que é formado por algumas componentes separadas pelo carácter `$`. No bcrypt, a primeira componente denota a versão do algoritmo utilizado (ao longo da história, o bcrypt evoluiu em algumas versões, sendo a atual a `2b`). A segunda componente denota o valor do parâmetro de custo utilizado para a geração do *hash*. Finalmente, a última componente contém o valor do *salt* utilizado concatenado com o valor do *hash* resultante.

Nota-se, portanto, que mesmo o código acima não tendo especificado um *salt*, o próprio *bcrypt* realiza o sorteio de um aleatoriamente informando-o na sua saída. Isto explica também porque cada uma das três execuções sucessivas do mesmo programa resultaram em saídas distintas: cada uma utilizou um *salt* diferente, o que também implica em valores finais do *hash* distintos.

O pacote `bcrypt` também disponibiliza um segundo método denominado `compare()`. Como o nome sugere, este método "compara" uma *password* a um valor de *hash* previamente computado pelo `bcrypt`. Isto é utilizado, por exemplo, para a verificação da *password* provida pelo utilizador contra a base de informações de validação. Por exemplo:

```node
const storedHash = '$2b$12$CMwjUMW.PbsaZ1OY2V5GpOY7P4hEH8IJVmBMGhXMhg7009Kwpguwa';​
bcrypt.compare('changeit', storedHash, (err, result) => {​
  if (err) {​
    return console.error('Erro no cálculo:', err);​
  }​
  console.log('Palavra-passe correta:', result); // true ou false​
});
```

## Recomendações Atuais

Enquanto desenvolvedores de *software*, é sempre importante estarmos atentos às recomendações mais recentes de técnicas e algoritmos de segurança a serem utilizados, dada a constante evolução da área. Isto aplica-se, em particular, ao armazenamento de *passwords*, onde novas funções de *hash* surgem ao longo do tempo em resposta ao desenvolvimento de técnicas mais efetivas de ataque.

Neste sentido, a OWASP (*Open Web Application Security Project*), uma entidade sem fins lucrativos que provê recursos gratuitos relativos à segurança, mantém dentro da documentação que produz uma série de recomendações. Particularmente, a OWASP disponibiliza um *cheat sheet* à respeito das melhores práticas relativas a uma série de problemas de segurança informática, incluindo o armazenamento seguro de *passwords*: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#password-storage-cheat-sheet

Dentre as recomendações apresentadas, lista-se o algoritmo Argon2 como função da *hash* a ser utilizada (se possível). Esta função de *hash* criptográfica foi a vencedora do *Password Hashing Competition*, uma competição aberta com o objetivo de incentivar a criação e reconhecer as melhores funções de *hash* criptográficos para o propósito particular do armazenamento de *passwords*. O Argon2 está especificado na RFC 9106.

Embora estejamos a referir ao Argon2 como um único algoritmo, ele na verdade possui três versões. A recomendação mais específica da OWASP é a utilização da versão Argon2id. Além de outras propriedades interessantes, o Argon2id apresenta uma **significativa complexidade de espaço** -- em outras palavras, o cálculo da função da *hash* exige acesso a uma quantidade significativa de memória. 

Esta propriedade aumenta a resistência do Argon2id a ataques baseados em GPUs (*Graphical Processing Units*). GPUs possuem uma arquitetura de *hardware* altamente paralela, o que em tese permitiria computar milhares de *hashes* (para milhares de *passwords*) simultaneamente. Desta forma, o uso de GPUs acelera consideravelmente ataques de dicionário do tipo 1. Ao incluir um requisito significativo de memória, o Argon2id inviabiliza o uso de GPUs (ao menos com o nível de paralelismo que poderia ser alcançado para outras funções de *hash*).

O Argon2id possui três parâmetros: o tamanho de memória ser utilizado ($m$), o número mínimo de iterações ($t$) e um grau de paralelismo ($p$). Os parâmetros $m$ e $t$ controlam o custo de cálculo do *hash* -- valores maiores implicam execuções mais lentas. Já o parâmetro $p$ adequa a função a ser computada por processadores com múltiplos núcleos -- *i.e.*, permite que partes da função sejam computados em paralelo.

Dentro das recomendações da OWASP, encontram-se parametrizações mínimas do Argon2id. Especificamente, recomenda-se o uso de uma das seguintes parametrizações:

- m=47104 (46 MiB), t=1, p=1
- m=19456 (19 MiB), t=2, p=1 
- m=12288 (12 MiB), t=3, p=1
- m=9216 (9 MiB), t=4, p=1
- m=7168 (7 MiB), t=5, p=1

Estima-se que todas estas 5 alternativas provejam níveis similares de segurança, porém com compromissos diferentes entre complexidade de tempo e espaço (*i.e.*, tempo de execução *vs.* memória utilizada).

> [!NOTE]
> Ilustração do uso do Argon2id.
>
> Há várias implementações disponíveis do Argon2 que permitem calcular o *hash* de certas *strings* / *passwords*. Um exemplo de execução é mostrado a seguir:
> 
> ```bash
> echo -n "mypassword" | argon2 somesalt -id -t 5 -k 7168
> Type:		Argon2id
> Iterations:	5
> Memory:		7168 KiB
> Parallelism:	1
> Hash:		778e133b77dcef8ff9e2dd32849d0efa5c3eee122a86331f491feee15ed5bbae
> Encoded:	$argon2id$v=19$m=7168,t=5,p=1$c29tZXNhbHQ$d44TO3fc74/54t0yhJ0O+lw+7hIqhjMfSR/u4V7Vu64
> 0.025 seconds
> Verification ok
> ```
>
> - Neste exemplo, solicitamos o cálculo da função de *hash* Argon2id (opção `-id`) com 5 iterações e 7168 kiB de memória (segundo recomendação da OWASP). Também especificamos o *salt* a ser utilizado (especificado na forma da string `"somesalt"`). A *password* de entrada é a `"mypassword"`.
> - Como saída, o programa imprime uma série de informações, mas os dois pontos principais são:
>   - `Hash`: mostra o valor do *hash* como um número hexadecimal.
>   - `Encoded`: mostra o *hash* codificado junto de uma série de outras informações de controlo, como o nome da função de *hash* e os parâmetros utilizados.

