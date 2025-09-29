[Changelog]: # (v0: versão inicial por Diego Passos)

# Aula 07 - Assinatura Digital e Introdução ao JCA

Na aula anterior, começamos a estudar esquemas e primitivas criptográficas assimétricas. Vimos que esta família de métodos caracteriza-se pelo uso de chaves distintas para operações criptográficas distintas. Em particular, uma das chaves é dita uma **chave pública** e pode ser conhecida por todos - inclusive eventuais atacantes - sem risco para a segurança do esquema. Por outro lado, há também a chave privada que, como o nome sugere, deve ser mantida em segredo pela entidade que a gera. Vimos também como utilizar as primitivas assimétricas em esquemas de cifra - neste caso, cifras assimétricas. Nestes esquemas, as mensagens são cifradas com a **chave pública do receptor**. Ao receber a mensagem cifrada, o receptor as decifra com a sua própria chave privada.

Lembre-se, ainda, que em aulas anteriores estudamos os esquemas MAC: esquemas criptográficos simétricos que visam garantir a integridade / autenticidade das mensagens.

Na primeira parte desta aula, estudaremos as **assinaturas digitais**. Trata-se de esquemas criptográficos **assimétricos** para garantir integridade / autenticidade. Dito de outra forma, as assinaturas digitais são a contraparte assimétrica dos esquemas MAC. Embora ambos os métodos tenham o mesmo objetivo geral, não se trata de duas alternativas totalmente equivalentes: veremos que há vantagens e desvantagens em cada abordagem.

Além disto, na segunda metade da aula, daremos início ao estudo do JCA (*Java Cryptography Architecture*). O JCA é parte da API de segurança padronizada da linguagem Java e oferece acesso programático a uma série de primitivas e esquemas criptográficos. Desta forma, podemos utilizar o JCA para escrever programas em Java que implementem protocolos criptográficos utilizando diversos esquemas de cifra, MAC e assinatura digital.

## Assinatura Digital: Introdução

Se analisarmos de maneira abstrata o uso das primitivas criptográficas simétricas e assimétricas, podemos notar que há uma diferença importante na relação entre as chaves utilizadas e os participantes na comunicação. Ao utilizarmos uma primitiva simétrica, há uma única chave que é utilizada por todos os participantes legítimos da comunicação. Desta forma, uma dada chave simétrica está associada a uma comunicação, e não aos seus participantes individualmente. 

Por outro lado, nas primitivas assimétricas utiliza-se um par de chaves, mas este par está associado especificamente a uma das entidades da comunicação. Por exemplo, no cenário didático de comunicação entre Alice e Bob, Bob cifra a mensagem que deseja enviar à Alice utilizando a **chave pública da Alice**. Ao receber a mensagem, Alice a decifra com a **sua própria chave privada**. Analogamente, se agora Alice deseja enviar uma resposta cifrada a Bob, ela o faz cifrando sua resposta com a **chave pública de Bob**. Por sua vez, Bob decifra a resposta utilizando **sua própria chave privada**.

Podemos, portanto, pensar no par de chaves pública e privada de uma determinada entidade como uma **identidade digital**. Ou seja, algo que identifica unicamente aquela entidade. 

Esta ideia fundamental pode ser explorada para o propósito de verificação de integridade / autenticidade de mensagens. Isto dá origem ao esquema criptográfico de **assinatura digital**.

A ideia básica da assinatura digital é similar à dos esquemas MAC: gerar algum tipo de marca a partir de uma mensagem que permita sua posterior verificação por um receptor. Entretanto, enquanto a marca gerada por um esquema MAC está intrinsecamente associada à chave simétrica partilhada entre as partes, a marca da assinatura digital está associada ao par de chaves pública e privada da entidade assinante.

Note que, num processo de assinatura digital, há duas fases: a geração da assinatura (ou seja, a marca) e a posterior verificação. Como expectável, a geração da assinatura deve ser realizada pela entidade que originou a mensagem. Por outro lado, gostaríamos que qualquer outra entidade fosse capaz de realizar a verificação da autenticidade da assinatura. Desta forma, **a verificação (feita por qualquer entidade) só pode se dar através da chave pública**. Ao mesmo tempo, se queremos que apenas a entidade legítima seja capaz de assinar determinada mensagem, a **assinatura deve ser feita com a chave privada**. Repare que isto é exatamente o **inverso do que ocorre nas cifras assimétricas**.

Outra característica peculiar da assinatura digital em relação aos esquemas MAC é durabilidade das chaves assimétricas. Conforme discutido em aulas anteriores, esquemas de cifra simétrica frequentemente utilizam **chaves de sessão**, *i.e.*, chaves efêmeras estabelecidas para uma sessão específica de comunicação e depois esquecidas. Por este motivo, uma marca gerada por um esquema MAC computada sobre uma chave de sessão tende a só ser útil durante aquela sessão, perdendo a capacidade de verificabilidade a longo prazo. Chaves assimétricas, por outro lado, são utilizadas por períodos tipicamente bem mais longos (anos, talvez décadas). Assim, é possível verificar assinaturas digitais mesmo que geradas há anos atrás. Isto faz com que assinaturas digitais sejam particularmente interessantes para garantir a integridade / autenticidade de documentos utilizados múltiplas vezes e a longo prazo (*e.g.*, um contrato de uma transação financeira).

Uma questão importante ao discutir-se o esquema de assinatura digital é a obtenção da **chave pública correta** para a verificação da assinatura. Suponha que uma atacante, Eva, envia uma mensagem a Bob passando-se por Alice. Como Eva não conhece a chave privada de Alice, ela não deve ser capaz de gerar uma assinatura correta para a mensagem forjada. No entanto, o que impede Eva de gerar um par de chaves pública e privada arbitrárias, assinar a mensagem forjada com esta chave privada e enviar a chave pública a Bob dizendo ser a chave de Alice? 

A resposta para isto é que é necessário algum esquema seguro para a disseminação das chaves públicas, de forma a garantir que as entidades legítimas (no exemplo, Bob) possam ter certeza de que possuem as chaves públicas corretas (no caso, de Alice). A solução mais amplamente adotada para isto é o uso de **certificados digitais**, algo que estudaremos em detalhes em aulas futuras. Nesta aula, ignoraremos temporariamente este problema e assumiremos que, de alguma forma, as partes legítimas conhecem as chaves públicas corretas umas das outras.

## Assinatura Digital - Visão Geral

A arquitetura geral de uma assinatura digital é muito similar à de um esquema MAC. De um lado da comunicação, a entidade que origina a mensagem executa um processo de **assinatura**. Este processo dá origem a uma sequência de bits denominada **assinatura digital** que é análoga à marca gerada pelos esquemas MAC. A entidade de origem, então, anexa a assinatura à mensagem e envia ambos pelo canal inseguro. 

Do lado receptor, executa-se uma função de verificação. Como nos esquemas MAC, esta função recebe como entradas a mensagem e a assinatura, e retorna um valor binário indicando se a mensagem é válida ou inválida (ou, mais precisamente, se é íntegra / autentica ou não).

Além da nomenclatura diferente (assinatura digital *vs.* marca), os esquemas MAC e as assinaturas digitais diferem também pelo uso das chaves. Como esquemas MAC são simétricos, ambos os lados - geração e verificação da marca - utilizam a mesma chave simétrica. Por outro lado, a assinatura digital é um esquema assimétrico e, portanto, chaves diferentes são utilizadas em cada lado. Como o objetivo da assinatura é atrelar a mensagem à sua origem, a geração da assinatura é feita com a chave privada da entidade de origem. Por outro lado, **a verificação se dá com a chave pública da entidade de origem**.

## Assinatura Digital: Formalização

De maneira mais formal, um esquema de assinatura digital é composto por três funções: uma função $G(.)$ de geração de chaves, uma função $S(.)$ de assinatura e uma função $V(.)$ de verificação da assinatura.

A função $G(.)$ de um esquema de assinatura digital é equivalente à função homônima que definimos na última aula para as cifras assimétricas. Trata-se, portanto, de uma função probabilística - dado que a chave gerada deve ser imprevisível para um atacante - mas que necessita gerar um par de chaves (pública e privada) que "façam sentido" em conjunto. Dito de outra forma: embora seja necessário haver componentes aleatórios na escolha da chave, as chaves pública e privada devem ser relacionadas de acordo com as propriedades necessárias ao funcionamento da primitiva assimétrica utilizada. No caso do RSA, em particular, isto significa obedecerem às relações adequadas dos valores $N$, $E$ e $D$, conforme estudamos na aula anterior.

A função $S(.)$ recebe como entradas a chave privada e a mensagem $m$ a ser assinada, e tem como saída uma assinatura $s$. Assim como ocorria nos esquemas MAC, é importante que a função $S(.)$ seja capaz de processar mensagens de tamanho arbitrário ($`m \in \{0,1\}^*`$). Por outro lado, é desejável que as assinaturas retornadas pela função tenham comprimento fixo e, idealmente, relativamente pequeno (ao menos, em comparação com as mensagens potencialmente grandes que se pode desejar assinar).

Já a função $V(.)$ recebe como entradas a assinatura $s$, a mensagem $m$ e a chave pública da entidade que supostamente a originou. Como saída, tem-se um valor booleano que indica se a assinatura corresponde ou não à mensagem e a origem.

## Assinatura Digital: Implementação

Mas como concretamente podemos implementar uma assinatura digital? O que exatamente fazem as funções $S(.)$ e $V(.)$?

Embora diferentes alternativas já tenham sido propostas como métodos de assinatura digital, no método mais comumente utilizado atualmente a assinatura digital é simplesmente o resultado da aplicação da **função de cifra** de uma primitiva assimétrica utilizando-se a **chave privada** a um ***hash* criptográfico** da mensagem. Matematicamente: $s = S(k_{priv})(m) = E(k_{priv})(H(m))$. Em termos mais simples:

1. Calcula-se o *hash* criptográfico $h$ da mensagem $m$ (utilizando alguma função de *hash* $H(.)$ segura).
2. Aplicação a função de cifra $E(.)$ da primitiva ao *hash* $h$, utilizando-se a chave privada.

Repare que, no processo de verificação, não se pode adotar a mesma estratégia utilizada nos esquemas MAC. Lembre-se que, num esquema MAC, o receptor basicamente tenta gerar uma marca a partir da mensagem recebida (e da chave simétrica) e depois compara a marca recebida com a mensagem àquela que ele próprio computou. Isso não é possível no caso da assinatura digital porque o receptor da mensagem não deve conhecer a chave privada da origem, pelo que não conseguirá reproduzir a assinatura.

No entanto, como estamos a utilizar um esquema assimétrico, o receptor pode fazer uso da chave pública da origem. Mais especificamente, o receptor:

1. Começa por aplicar a função de decifra $D(.)$ da primitiva sobre a assinatura recebida, utilizando a chave pública da origem. Isto deve dar origem a um valor de *hash* $h' = D(k_{pub})(s)$. 
2. Depois, calcula ele próprio o *hash* da mensagem $m'$ recebida utilizando a mesma função de *hash* usada para gerar a assinatura. Disto, resulta um $h'' = H(m')$.
3. Por fim, compara $h'$ e $h''$. Se $h' = h''$, a assinatura recebida é consistente com a mensagem e, portanto, declara-se a mensagem íntegra e autentica. Caso contrário, declara-se a mensagem inválida (ou foi gerada por uma entidade que não é a origem esperada, ou foi alterada de alguma forma).

## Propriedades de um Esquema de Assinatura Digital

Devido aos seus objetivos semelhantes, assinaturas digitais e esquemas MAC partilham diversas propriedades.

Em particular, a propriedade de correção de uma assinatura digital é análoga à propriedade de correção de um esquema MAC:


$$
\forall m \in \lbrace 0,1\rbrace^*, \forall (k_{priv}, k_{pub}) \in KeyPairs: V(k_{pub})(S(k_{priv})(m), m) = true
$$

Em termos mais simples, isto significa que a verificação de uma assinatura digital correta para uma mensagem legítima deve sempre ser positiva. Dito de outra forma, não queremos que a função de validação erroneamente classifique uma mensagem íntegra e autentica como ilegítima. 

No entanto, como já ocorrera para os esquemas MAC, repare que esta definição de correção **não garante que não haverá falsos positivos**. Isto é, segundo esta definição, um esquema de assinatura digital correto pode eventualmente classificar como íntegra e autentica uma mensagem forjada ou adulterada de alguma maneira.

Esta tolerância a falsos positivos é uma consequência direta da utilização de *hashes* criptográficos como parte do processo de assinatura digital. Como, para qualquer função de *hash*, toda mensagem $m$ tem infinitas segundas pré-imagens, é teoricamente possível que uma mensagem adulterada $m'$ tenha exatamente o mesmo *hash* que uma mensagem legítima $m$ e, portanto, exatamente a mesma assinatura digital.

Lembre-se, no entanto, que, conforme estudado nas últimas aulas, para boas funções de *hash* criptográfico deve ser computacionalmente infazível determinar esta segunda pré-imagem. Igualmente, encontrar colisões também deve ser computacionalmente infazível. Além disso, a probabilidade de que duas mensagens quaisquer partilhem um mesmo valor de *hash* - e, portanto, uma mesma assinatura - deve ser muito baixa.

Assim, mesmo que teoricamente exista a possibilidade de falsos positivos, considera-se um esquema de assinatura digital **seguro** se, sem o conhecimento da chave privada $k_{priv}$ for computacionalmente infazível:

- Realizar uma **falsificação seletiva**. Isto é, dada uma mensagem $m$ **específica**, encontrar uma assinatura $s$ tal que $V(k_{pub})(s, m) = true$.
- Realizar uma **falsificação existencial**. Isto é, encontrar um par qualquer $(m, s)$ tal que $V(k_{pub})(s, m) = true$.

Estas propriedades dependem tanto da qualidade da função de *hash* criptográfico utilizada quanto da segurança das primitivas assimétricas escolhidas.

Dado que a assinatura $s$ é geralmente resultado da aplicação de uma função de *hash* seguida da cifra por um método assimétrico, as assinaturas tem tipicamente comprimento fixo. O comprimento exato de uma assinatura depende de uma série de particularidades do método de assinatura utilizado, incluindo qual primitiva criptográfica e qual o tamanho das chaves utilizados. Exemplos de comprimentos tipicamente encontrados em aplicações modernas são 1024, 2048 ou 4096 bits.

Destaque-se, ainda, que, por envolverem o cálculo das funções de cifra e decifra de primitivas assimétricas, as assinaturas digitais têm custo computacional tipicamente bem mais elevado que os esquemas MAC - estes últimos baseados em simples funções *hash*.

Por fim, é importante que fique claro que, embora utilizem as funções de cifra e decifra da primitiva assimétrica, assinar uma mensagem **não é equivalente** a decifrar uma mensagem e verificar uma assinatura **não é equivalente** a cifrar uma mensagem. Não só os propósitos das operações são diferentes, mas também deve-se lembrar do uso das funções *hash*, que não são utilizadas nos esquemas de cifra assimétrica.

## Assinatura Digital: Arquitetura Interna

Já falamos nesta aula sobre como os esquemas de assinatura digital combinam funções de *hash* com primitivas de cifra e decifra assimétricas para efetuar as operações de assinatura e verificação. No entanto, até aqui ignoramos um aspeto importante do ponto de vista prático: a **formatação**.

O conceito de formatação já foi discutido anteriormente nesta UC no contexto das cifras assimétricas. Naquela altura, verificamos que o mapeamento de uma mensagem para o domínio dos números inteiros - que, em geral, é o domínio da função de cifra de uma primitiva assimétrica - não era trivial e envolvia operações de *padding* e inclusão de bits pseudo-aleatórios. Este era justamente o papel dos métodos de formatação.

O mesmo princípio se aplica ao caso da assinatura digital. Dada a escolha da função de *hash*, sua saída é uma sequência de bits de um determinado comprimento. Tipicamente, este comprimento (*e.g.*, 128, 256, 512 bits) é significativamente menor que o comprimento da chave utilizada na primitiva assimétrica. Assim, algum método de formatação é necessário para mapear de maneira não ambígua o valor do *hash* para um elemento do domínio da função de cifra. Analogamente, durante a verificação, é necessário realizar a desformatação ao decifrar a assinatura, obtendo de volta o valor correto do *hash* cifrado.

Assim, um esquema de assinatura digital precisa definir três componentes:

1. Uma função de *hash* criptográfico (*e.g.*, MD5, SHA-1, SHA-256).
2. Um método de formatação (*e.g.*, PKCS#1 v1.5, PSS, X9.31).
3. Uma primitiva assimétrica (*e.g.*, RSA, DSA).

Isto traz uma flexibilidade significativa para os esquemas de assinatura digital, já que permite a escolha de métodos específicos para cada um destes três componentes de maneira praticamente independente. Assim, por exemplo, uma mesma primitiva (*e.g.*, RSA) pode ser utilizada com diferentes métodos de formatação e funções de *hash*. Logo, se, digamos, uma dada função de *hash* torna-se obsoleta, basta substituí-la por outra, mantendo os demais componentes fixos. Uma restrição, no entanto, é que a saída da função de *hash* deve ter um comprimento menor que o módulo $N$ da chave assimétrica. Isto, no entanto, é, em geral, verdade.

Como uma última observação, repare que, ao contrário dos esquemas MAC, embora a assinatura digital envolva chaves e funções de *hash*, as chaves são utilizadas apenas pela primitiva assimétrica.

> [!NOTE]
>    Geração e verificação de assinaturas digitais com o `openssl`.
>
>    - Assinar um ficheiro testo qualquer:
>
>    ```bash
>    $ openssl dgst -sign alice_private_key.pem -sha256 -out assinatura.sig mensagem.txt
>    ```
>
>    - Exibir conteúdo do ficheiro que contém a assinatura:
>
>    ```bash
>    $ hexdump -C assinatura.sig 
>    00000000  4c 4a f5 9c e0 78 b9 0f  b9 c4 6c 9d 40 2e 24 0a  |LJ...x....l.@.$.|
>    00000010  ad 8e e2 08 b7 ab 9b 2a  e3 b2 3b b6 28 a5 9d ff  |.......*..;.(...|
>    00000020  48 bb 45 ce 88 de 28 ca  96 c7 e2 e2 24 1a 9a d6  |H.E...(.....$...|
>    00000030  39 23 59 d8 f4 6c 6d cc  9c 62 1a 07 a3 3c 79 e4  |9#Y..lm..b...<y.|
>    00000040  b4 a3 25 1e 65 7d 94 43  82 dd a2 49 13 78 bf ab  |..%.e}.C...I.x..|
>    00000050  31 ab 55 fb f2 6e 3f 24  53 26 42 e9 99 9c ec 45  |1.U..n?$S&B....E|
>    00000060  80 04 d8 7b 6b c5 49 17  99 b4 12 45 23 5c c9 42  |...{k.I....E#\.B|
>    00000070  1b 5c 33 f8 02 09 6e 39  f8 41 b5 c0 ce c0 03 45  |.\3...n9.A.....E|
>    00000080  b0 ac 7a ed fe f6 26 e0  90 b4 95 67 79 4b 58 69  |..z...&....gyKXi|
>    ...
>    ```
>
>    - Mostrar processo de verificação:
>
>    ```bash
>    $ openssl dgst -verify alice_public_key.pem -sha256 -signature assinatura.sig mensagem.txt
>    Verified OK
>    ```
>
>    - Destacar que verificação foi bem sucedida.
>    - Modificar o ficheiro e realizar a verificação novamente:
>
>    ```bash
>    $ openssl dgst -verify alice_public_key.pem -sha256 -signature assinatura.sig mensagem_modificada.txt 
>    Verification failure
>    40E7C8EC727F0000:error:02000068:rsa routines:ossl_rsa_verify:bad signature:../crypto/rsa/rsa_sign.c:430:
>    40E7C8EC727F0000:error:1C880004:Provider routines:rsa_verify:RSA lib:../providers/implementations/signature/rsa_sig.c:774:
>    ```

## JCA: Introdução

A JCA (*Java cryptography Architecture*) é uma API criptográfica padronizada para a linguagem Java. Ela fornece acesso à esquemas criptográficos e funções auxiliares para a execução de tarefas criptográficas básicas, como cifras, MACs, *hashes*, assinaturas, geração de chaves e geração de números pseudo-aleatórios. Com base na JCA, pode-se construir protocolos criptográficos completos para aplicações / sistemas informáticos.

Dois pontos-chave do desenho da JCA são sua independência de implementação e de algoritmos. **Independência de algoritmos** significa que a JCA fornece uma API genérica para o acesso aos principais tipos de esquemas e tarefas criptográficas, de forma que a **estrutura do código** que utiliza a JCA permanece muito similar - quase idêntica - mesmo se utilizarmos algoritmos diferentes. Por exemplo, suponha um programa em JCA que realize a cifra de ficheiros especificados pelo utilizador, gerando o texto cifrado como sua saída. Versões deste programa utilizando a cifra DES-EBC ou AES-CBC podem ser praticamente idênticas, diferindo, possivelmente, em uma única linha (ou mesmo, em uma única *string*). 

A independência de algoritmo é uma característica muito positiva da JCA, tendo-se em vista que algoritmos criptográficos tornam-se obsoletos com o tempo. Logo, um código em JCA tende a ser naturalmente genérico e de fácil adaptação para o uso de outros / novos algoritmos.

Por outro lado, a **independência de implementação** significa que a JCA também é genérica em termos de qual implementação de um dado algoritmo é utilizada. Mais concretamente, a JCA permite a escolha entre várias implementações alternativas de um mesmo algoritmo (digamos, uma cifra AES-GCM) através de alterações pontuais no código da aplicação. Mais especificamente, a implementação particular de um esquema pode ser escolhida manipulando-se um único parâmetro de um método, sem que isto afete de qualquer forma o restante do código. Aliás, em certas situações, é possível alterar a implementação utilizada apenas através de ficheiros de configuração.

Como no caso da independência de algoritmo, a independência de implementação é um recurso extremamente útil dado que implementações outrora consideradas seguras podem vir a tornar-se inseguras (*e.g.*, uma vez que um *bug* ou vulnerabilidade seja descoberta). Assim, a habilidade de facilmente chavearmos entre implementações diferentes de um mesmo algoritmo é valiosa.

Além disto, a JCA também garante a interoperabilidade entre estas implementações. Isto significa, por exemplo, que é possível gerar a assinatura digital de um documento com uma implementação de determinado esquema e depois fazer a verificação da mesma com outra implementação diferente. Isto é essencial ao passo que garante a interoperabilidade entre aplicações que correm em sistemas ou plataformas diferentes.

Estas características de independência de algoritmos e implementação significam que podemos entender a JCA como uma estrutura hierárquica. Do ponto de vista das classes manipuladas pela aplicação, são utilizadas interfaces genéricas para esquemas criptográficos (*e.g.*, cifra), independentemente do algoritmo particular escolhido. Abaixo destas interfaces genéticas encontram-se classes particulares que definem cada algoritmo (*e.g.*, DES-CBC com *padding* PKCS#5). Finalmente, no último nível, temos as implementações concretas, que podem ser múltiplas para cada algoritmo.

## JCA: Arquitetura

Há três componentes fundamentais da arquitetura da JCA: os Cryptographic Service Providers (ou CSPs), as *Engine Classes* e as *Specification Classes*.

Os ***Cryptographic Service Provider*** são *packages* ou classes que provêm implementações concretas de um ou mais serviços criptográficos. Nas versões do JDK (*Java Development Kit*) distribuídas pela Oracle, são fornecidos *providers* como o `Sun`, o `SunRsaSign`, e o `SunJCE`. No entanto, a JCA é flexível e permite a implementação de outros *providers*. Mesmo que não queiramos nós próprios implementar um *provider*, é possível instalar *providers* de terceiros no ambiente de execução da aplicação para utilização com nossas aplicações.

As ***Engine Classes*** são o cerne da API JCA da perspectiva da aplicação. Trata-se de um conjunto de classes que representam de maneira abstrata esquemas criptográficos e outras funcionalidades acessórias. Estas classes não implementam de facto os esquemas criptográficos - isto é feito pelos *providers*. No entanto, elas provêm uma interface padronizada para a seleção e manipulação de tais esquemas, independente dos algoritmos particulares desejados. 

A utilização de uma *Engine Class* começa através da chamada ao método estático `getInstance()`. Este método recebe um ou mais argumentos que, entre outras coisas, especificam os algoritmos criptográficos a serem utilizados. Como consequência, a `getInstance()` retorna um objeto da *Engine Class* que permite a execução das tarefas associadas ao esquema criptográfico solicitado.

As *Engine Classes* têm por característica serem **opacas**. Isto significa que estas classes expõem apenas funções e comportamentos necessários à execução das tarefas criptográficas (*e.g.*, um método que realiza a verificação de uma assinatura digital). Detalhes internos, como os valores das chaves utilizadas, são totalmente escondidos por estas classes.

Por vezes, no entanto, precisamos de acesso a estes detalhes, seja para lê-los (por exemplo, descobrir o IV utilizado para cifrar uma mensagem de forma a enviá-lo para a outra parte legítima da comunicação) ou para especificá-lo (por exemplo, usar um valor de chave específico recebido da outra parte). 

Nestes casos, são utilizadas as ***Specification Classes***. Estas classes fornecem representações transparentes de parâmetros dos esquemas criptográficos (por exemplo, chaves, IVs). A transparência significa que a aplicação pode solicitar a leitura de detalhes daquele elemento (*e.g.*, os valores de $P$ e $Q$ que deram origem a uma chave RSA). Podemos também fazer o sentido inverso: preencher uma *specification class* com informações por nós especificadas. Em geral, as *specification Classes* provêm métodos que retornam estas informações acerca dos parâmetros em formatos padronizados que podem ser utilizados por outros sistemas.

## *Providers*

A implementação de um *provider* consiste na criação de classes que implementam as classes abstratas `<EngineClass>Spi`. Estas classes abstratas correspondem às *Engine Classes* que discutimos há pouco. Assim, por exemplo, para a *Engine Class* `Cipher` há uma classe abstrata `CipherSpi` que define quais métodos devem ser implementados por um *provider* para aquela tarefa criptográfica. 

Além de prover uma classe concreta que implementa classes abstratas `<EngineClass>Spi`, *providers* devem também disponibilizar uma classe que herda da classe `Provider`. A classe `Provider` definida pelo JCA funciona como uma base de algoritmos suportados por cada provider. Através de métodos disponíveis nesta classe, o JCA é capaz de determinar se certa combinação de esquema / algoritmo é provida por um determinado *provider*. Se for, a classe `Provider` dá, ainda, a informação de qual classe concreta do *provider* corresponde à classe `<EngineClass>Spi` necessária.

Enquanto utilizadores da JCA, implementar um *provider* não deve ser uma preocupação frequente. Na verdade, em geral, simplesmente utilizamos os *providers* já incluídos no JDK do ambiente de execução da aplicação. Todo JDK inclui *providers* que disponibilizam um grande conjunto de algoritmos e que são suficientes para a maior parte dos usos. 

No entanto, em certas situações específicas, podemos necessitar utilizar *providers* além dos incluídos na distribuição do JDK. Possíveis razões podem incluir:

- a eventual existência de *providers* com implementações significativamente mais eficientes (em termos de tempo de execução ou uso de recursos) que os incluídos no JDK;
- a eventual existência de *providers* nos quais temos maior nível de confiança por um motivo ou por outro;
- a existência de *providers* que implementam algoritmos diferentes daqueles disponibilizados pelos *providers* incluídos no JDK;
- eventuais restrições legais quanto a utilização de implementações criptográficas certificadas por determinadas entidades;
- eventuais restrições legais quanto a utilização de implementações criptográficas baseadas em *software* desenvolvido em determinados países; ou
- a recomendação de uso de um *provider* desenvolvido na própria *software warehouse* (ou numa parceira).

Seja qual for a motivação, *providers* podem ser facilmente instalados no ambiente de execução de uma aplicação Java. A instalação passa pela colocação do *package* do *provider* na *classpath* da JVM (*Java Virtual Machine*) no ambiente de execução ou, alternativamente, na diretoria de extensões. 

Feito isto, é necessário também **registar** o *provider* junto ao JCA. Este registo pode ser feito de duas maneiras. A primeira é através do ficheiro `java.security`, que encontra-se em um caminho específico dentro da diretoria de instalação do Java no sistema. Trata-se de um ficheiro texto de configuração que define uma série de parâmetros de comportamento relativos à segurança. Entre outras coisas, este ficheiro regista uma lista de *providers* associadas de respetivas prioridades. Um trecho de um ficheiro `java.security` pode ser visto a seguir:

```
...
#
# List of providers and their preference orders (see above):
#
security.provider.1=SUN
security.provider.2=SunRsaSign
security.provider.3=SunEC
security.provider.4=SunJSSE
security.provider.5=SunJCE
security.provider.6=SunJGSS
security.provider.7=SunSASL
security.provider.8=XMLDSig
security.provider.9=SunPCSC
security.provider.10=JdkLDAP
security.provider.11=JdkSASL
security.provider.12=SunPKCS11
...
```

Se quiséssemos registar um novo *provider* neste ambiente, bastaria adicionarmos uma nova linha ao final deste trecho com o conteúdo `security.provider.13=<nome provider>`.

Alternativamente, podemos fazer o registo dinâmico de um *provider*. Ou seja, independentemente do conteúdo fo ficheiro `java.security` podemos, programaticamente, solicitar o registo de um *provider*. Isto é feito através de uma classe especial da JCA chamada `Security`. Esta classe fornece métodos tanto para o registo de *providers* quanto para obtermos uma lista dos *providers* atualmente registados e dos algoritmos por eles disponibilizados.

Note no exemplo anterior do ficheiro `java.security` que há números associados a cada entrada da lista de *providers*. Por exemplo, o *provider* `SUN` é listado como número 1, enquanto o *provider* `XMLDSig` é o de número 8. Estes números denotam uma **ordem de prioridade** ou de preferência dos *providers*, o que é utilizado pelo JCA quando solicitamos a instanciação de um objeto de uma das *Engine Classes*.

Considere, por exemplo, uma solicitação de instanciação de um objeto da *EngineClass* `MessageDigest` - *i.e.*, um objeto para a manipulação de funções de *hash* criptográfico. Esta instanciação pode ser realizada através da seguinte linha de código:

```Java
md = MessageDigest.getInstance("SHA-256");
```

Qual dos 12 *providers* listados na versão acima do ficheiro `java.security` seria utilizado? Para responder isto, o JCA faria um varrimento dos 12 *providers* disponíveis **exatamente na ordem de prioridade** indicada no ficheiro e, para cada um, verificaria se o algoritmo solicitado (neste caso, o `SHA-256`) é implementado. A busca termina no momento em que o JCA encontra o primeiro *provider* para o algoritmo. Dito de outra forma: o JCA seleciona sempre o *provider* de mais alta prioridade que implementa o algoritmo solicitado.

Se, por qualquer motivo, quisermos utilizar um *provider* específico - ao invés daquele que seria retornado naturalmente pela busca do JCA -, temos duas alternativas. Uma é simplesmente alterarmos a lista de prioridades: podemos livremente alterar o ficheiro `java.security` mudando, em particular, a ordem de preferência dos *providers* registados. Uma outra forma mais simples é mudarmos ligeiramente a linha de código de instanciação do `MessageDigest`:

```Java
md = MessageDigest.getInstance("SHA-256", "ProviderC");
```

Neste caso, utilizamos uma versão alternativa do método `getInstance()` que recebe dois parâmetros. Além da especificação do algoritmo, o segundo argumento especifica o **nome do *provider*** desejado. Neste caso, o JCA tentará encontrar (e instanciar) este *provider* especificamente. Caso o *provider* informado não exista ou não implemente o algoritmo solicitado, uma exceção é levantada.

De forma geral, a menos que tenhamos uma boa razão para fazê-lo, o mais recomendado é **não** especificarmos um *provider* na instanciação das *EngineClasses*. Há ao menos duas razões para isto:

1. Isto aumenta a portabilidade da aplicação, ao passo que não gera uma dependência da aplicação com um *provider* específico que pode não estar disponível em todo ambiente de execução. 
2. Isto simplifica a atualização de *providers* que passem a ser considerados vulneráveis ou obsoletos de alguma maneira. Se, por exemplo, especificamos no código-fonte o uso de um determinado provider `providerC` e futuramente for descoberta uma vulnerabilidade no mesmo, seríamos obrigados a alterar o código-fonte e recompilá-lo para permitir a migração para outro *provider* mais seguro. Se, por outro lado, não especificamos um *provider*, delegamos ao JCA a escolha, fazendo com que uma simples atualização do sistema onde a aplicação corre ser suficiente para substituir o *provider* vulnerável.

## Um Exemplo de Código JCA

Antes de discutirmos cada uma das classes e funcionalidades providas pelo JCA, é interessante vermos um exemplo completo de um trecho de código JCA para um propósito específico. Consideraremos aqui, a título de ilustração, um exemplo de uma pequena função/método que calcula um *hash* `SHA-256` sobre uma mensagem `m` recebida como parâmetro:

```Java
public static void exemploHash(String m) throws Exception {

    System.out.println("-----Exemplo Hash-----");
    System.out.println("m: " + m);

    // Obter instancia do message digest SHA256
    MessageDigest md = MessageDigest.getInstance("SHA256");

    // Solicitar computacao do hash da mensagem
    byte[] s = md.digest(m.getBytes());

    // Imprimir conteudo do hash no ecra (como numero hexadecimal)
    System.out.printf("hash: ");
    prettyPrint(s);
}
```

O método começa por imprimir algumas mensagens de *debug*, incluindo o conteúdo da mensagem recebida como parâmetro. Apenas duas linhas são diretamente relacionadas ao uso do JCA: na primeira, solicita-se uma instância de um `MessageDigest` utilizando-se o método estático `getInstance()` desta *Engine Class*. Repare na especificação do algoritmo `SHA-256` passada como argumento (na forma de uma *string*).

O objeto `md` retornado pode, então, ser utilizado para computar o *hash* da mensagem logo a seguir. Neste caso, utiliza-se o método `digest()` que recebe como argumento um *array* de bytes. Observe que a mensagem `m` encontra-se no formato de *string*. Entretanto, as operações de cifra, *hash*, MAC, etc da JCA são executadas sobre *arrays* de bytes. Logo, é necessária uma conversão (realizada com a chamada ao método `getBytes()` da *string*). Por fim, o conteúdo do *hash* retornado é impresso no ecrã utilizando-se uma função auxiliar `prettyPrint()` (que simplesmente imprime os valores hexadecimais de cada byte do *array* um a um, não tendo qualquer relação com a JCA).

Este trecho de código permite destacar uma série de características da JCA. Em primeiro lugar, é importante perceber a simplicidade da API: apenas duas linhas de código são efetivamente necessárias para o cálculo do *hash*. 

Além disto, vemos um exemplo de como a JCA alcança independência de algoritmo. Suponha por exemplo, que quiséssemos utilizar um *hash* `MD5` ao invés do `SHA-256` nesta função. A única alteração necessária seria na *string* passada como parâmetro para o método `getInstance()`. Todo o resto do código poderia ser mantido exatamente como está.

Outra característica evidenciada aqui é o uso do método `getInstance()`. Independentemente da tarefa criptográfica a ser realizada, em geral começamos por obter uma ou mais instâncias das *Engine Classes* necessárias através deste método.

Deve-se destacar também que as operações da JCA em geral são realizadas no domínio de `byte[]`. Isto deve ser levado em conta se o programa manipula mensagens no formato de *string*, além de nos momentos em que desejamos imprimir criptogramas, textos decifrados, *hashes*, assinaturas e marcas de autenticidade retornadas pelos métodos da API.

## *Engine Classes*

Vimos na seção anterior o uso de uma *Engine Class* particular do JCA: a `MessageDigest`. Entretanto, há várias outras *Engine Classes* que são utilizadas a depender do esquema criptográfico escolhido. Entre elas, podemos destacar:

- `Cipher`: utilizada em esquemas de cifra. É importante notar que trata-se de uma única classe utilizada tanto para esquemas simétricos quanto para os assimétricos.
- `Mac`: utilizada para esquemas MAC.
- `Signature`: utilizada para esquemas de assinatura digital.
- `MessageDigest`: utilizada para acesso a funções de *hash* criptográficas.
- `KeyGenerator`: utilizada para a geração de chaves simétricas.
- `KeyPairGenerator`: utilizada para a geração de pares de chaves assimétricas. 
- `SecureRandom`: utilizada para acesso a algoritmos de geração de números pseudo-aleatórios criptograficamente seguros.

Veremos várias destas classes em mais detalhes nesta e em próximas aulas, mas as últimas três desta lista merecem um breve comentário neste ponto. 

Tanto a `KeyGenerator` quanto a `KeyPairGenerator` fornecem acesso a funcionalidades de geração de chaves. Logo, estas classes são comumente utilizadas em conjunto com as classes `Cipher`, `Mac` e `Signature`, dados que os esquemas criptográficos associados utilizam alguma forma de chave. No exemplo, de código da seção anterior, em que utilizamos a classe `MessageDigest`, não necessitamos das classes `KeyGenerator` e `KeyPairGenerator` simplesmente porque funções de *hash*, por si só, não utilizam chaves. 

Já a classe `SecureRandom` tem papel fundamental na maioria dos esquemas criptográficos. Isto porque frequentemente necessitamos criar números ou sequências de números que **pareçam aleatórios**. Na prática, computadores por si só tem dificuldade em gerar números realmente aleatórios, dada a natureza determinística das suas instruções. No entanto, existem os chamados **algoritmos de geração de números pseudo-aleatórios**, *i.e.*, algoritmos que geram uma sequência de números que parecem aleatórios, embora sejam gerados através de algoritmos determinísticos. Além disto, muitas vezes são utilizadas **fontes de entropia** baseadas em grandezas aleatórias mensuráveis pelo computador (*e.g.*, o intervalo entre duas pressões consecutivas de teclas do teclado, flutuações na tensão de alimentação da fonte do computador). Em resumo, cabe aos *providers* da *Engine Class* `SecureRandom` implementar algoritmos e métodos que gerem sequências adequadas de valores pseudo-aleatórios. Estes valores são, depois, consumidos por outras *Engine Classes*, como as responsáveis pela geração de chaves.

Todas as *Engine Classes* disponibilizam várias sobrecargas de um método estático chamado `getInstance()`. Como já vimos anteriormente, este método tem por objetivo criar instâncias particulares da *Engine Class* com configurações específicas. A ideia, portanto, é utilizar um padrão de projeto *Factory* ao invés de permitir a instanciação direta da classe através do comando `new`. Esta arquitetura permite instanciar classes específicas (*i.e.*, para um algoritmo específico e de um *provider* específico) de maneira transparente ao utilizador, dado que o objeto retornado é sempre visto como sendo da classe genérica da *Engine Class*.

Na sua forma mais básica, o método `getInstance()` recebe como parâmetro apenas uma ***string* de transformação**. Por ora, podemos entender uma *string* de transformação como uma especificação do algoritmo que desejamos utilizar no esquema criptográfico solicitado. Há também sobrecargas deste método que nos permitem especificar um *provider* específico, como visto anteriormente. Esta especificação do *provider* pode ser realizada tanto na forma de uma *string* com o nome do *provider*, quanto através de um objeto da classe *Provider*.

Um detalhe relevante em determinadas situações é que, quando não especificamos um *provider*, a escolha deste **não é realizada imediatamente**. Ao contrário, a JCA adia esta decisão até que a chave a ser utilizada no esquema criptográfico seja informada. Isto permite ao JCA verificar características concretas da chave e escolher um *provider* de acordo. Por exemplo, o AES pode trabalhar com chaves de 128, 192 ou 256 bits. Eventualmente, um *provider* poderia disponibilizar apenas uma implementação do AES com chaves de, digamos, 128 bits. Assim, ao associarmos uma chave de 256 bits, a JCA perceberia que este *provider* é inadequado e selecionaria outro compatível.

## Exemplo com Cifra Simétrica

Podemos exemplificar o relacionamento entre as várias classes da JCA através de um cenário hipotético de cifra. 

Suponha que desejemos realizar uma **cifra simétrica**. Por se tratar de um esquema de cifra, devemos obter uma instância da *Engine Class* `Cipher`. Isto é feito através do método `getInstance()`, passando-se como argumento uma *string* de transformação que especifica o algoritmo desejado. Como discutiremos em maiores detalhes a seguir, esta *string* pode conter apenas o nome da primitiva (*e.g.*, `"AES"`) ou alternativamente a especificação de uma tupla no formato `"algoritmo/modo/padding"`, onde especifica-se, em ordem, o nome da primitiva, um modo de operação (*e.g.*, `CBC`) e um método de *padding* (*e.g.*, `PKCS5Padding`). 

Uma cifra que utiliza o modo CBC necessita de um IV que, geralmente, é sorteado aleatoriamente. Por este e outros motivos, internamente, um objeto `Cipher` tipicamente utiliza um objeto da classe `SecureRandom`. Além disto, por se tratar de uma cifra simétrica, é necessária a criação de uma chave simétrica. Esta geração pode ser realizada pela classe `KeyGenerator` que, para isto, acaba por também utilizar um objeto da classe `SecureRandom`. Como se trata de uma chave simétrica (e que, portanto, deve ser mantida em segredo), a chave retornada pela `KeyGenerator` é encapsulada num objeto da classe `SecreteKey`.

Esta inter-relação entre as várias classes fica mais clara com um exemplo concreto de código:

```Java
KeyGenerator keyGen = KeyGenerator.getInstance("AES");
// Opcional, se não passar um SecureRandom ao método init de keyGen
SecureRandom secRandom = new SecureRandom();
// Opcional
keyGen.init(secRandom);
SecretKey key = keyGen.generateKey();
// Gera o objeto da cifra simetrica
Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
// Associa a chave key a cifra
cipher.init(Cipher.ENCRYPT_MODE, key);
// Continuar com processo de cifra...
```

O exemplo começa pela instanciação do gerador de chave (*Engine Class* `KeyGenerator`). Como sempre, a instância é obtida pelo método *factory* `getInstance()`. Repare que, como algoritmos / esquemas diferentes podem necessitar de chaves com características diferentes, é necessário especificar para qual algoritmo é o `KeyGenerator`.

Em seguida, é feita a instanciação de um objeto da classe `SecureRandom` que posteriormente é repassado ao objeto da classe `KeyGenerator` através do método `init()`. Note que estes dois passos são opcionais: caso não façamos isto, a JCA se encarrega de selecionar internamente um gerador de números pseudo-aleatórios considerado seguro.

Uma vez iniciado o objeto da classe `KeyGenerator`, podemos solicitar a geração da chave utilizando para isto o método `generateKey()`. A chave retornada é da classe `SecretKey`.

O próximo passo é a instanciação da cifra em si. Novamente, utilizamos o método `getInstance()` - desta vez da *Engine Class* `Cipher`. Repare na *string* de transformação: `"AES/ECB/PKCS5Padding"`. Ela denota que solicitamos uma cifra baseada na primitiva AES utilizando o modo ECB e, como método de *padding*, o PKCS#5.

Antes de podermos realizar cifras ou decifras com o objeto recém-instanciado, devemos proceder à sua inicialização. Isto é feito através do método `init()`. Para um objeto do tipo `Cipher`, a inicialização permite a especificação do **modo de uso** e da chave - opcionalmente, podemos definir outros parâmetros, como o IV. O modo de operação diz basicamente qual operação desejamos daquele objeto (*e.g.*, cifra ou decifra) e é informado através de constantes definidas na *Engine Class* `Cipher` (aqui, por exemplo solicitamos o modo de cifra). Como chave, passamos o objeto `key` gerado nos passos anteriores.

A partir deste ponto, o objeto `Cipher` está pronto para uso.

## Transformações Normalizadas

Neste ponto, já citamos algumas vezes que o primeiro parâmetro do método `getInstance()` é uma *string* de **transformação**. Até aqui, no entanto, temos dito simplificadamente que esta *string* define o algoritmo a ser utilizado no esquema criptográfico solicitado. No entanto, como visto em exemplos anteriores, esta *string* carrega potencialmente mais informação que apenas o nome da primitiva desejada.

Na verdade, a *string* de transformação especifica - ou pode especificar - todo um esquema criptográfico. Isso tipicamente inclui o nome de uma primitiva ou função de *hash* (*e.g.*, `"AES"`, `"MD5"`), mas também pode incluir a especificação de métodos adicionais, como modos de operação ou *padding*. Em geral, para cada *engine class* há um determinado formato geral das *strings* de formatação suportadas. Alguns exemplos:

- Classe `Cipher`. Suporta dois formatos: "algorithm/mode/padding ou "algorithm". No primeiro caso, especifica-se completamente o esquema, enquanto no segundo especifica-se apenas a primitiva. Neste último caso, a própria JCA seleciona um modo de operação e *padding*. Diversas primitivas são suportadas (*e.g.*, `AES`, `DES`, `DESede`, `RSA`, ...), bem como modos (*e.g.*, `ECB`, `CBC`, `CFB`, `CTR`, `OFB`, ...) e estratégias de *padding* (*e.g.*, `PKCS5Padding`, `PKCS1Padding`, `OAEPPadding`, ...)
- Classe `Mac`. Nos casos de uso mais comuns, o formato geral é constituído do prefixo "Hmac" concatenado com o nome de uma função de *hash* (e.g., `MD5`, `SHA1`, `SHA256`, ...). Por exemplo, para um HMAC utilizando a função de *hash* SHA-256, a *string* de transformação seria `"HmacSHA256"`.
- Classe `Signature`. O formato geral é "<hash>with<primtiva>". Ou seja, concatena-se o nome de uma função de *hash*, o termo "with" e o nome de uma primitiva assimétrica. Alguns exemplos são: `"MD5withRSA"`, `"SHA256withRSA"`, `"SHA512withDSA"`.
- Classe `KeyGenerator`. Quando a chave a ser gerada é para uma cifra, a *string* deve conter o nome da primitiva (*e.g.*, `AES`). Quando a chave a ser gerada é para um esquema MAC, deve-se utilizar o mesmo formato usado para a Classe `Mac`.
- Classe `KeyPairGenerator`. Em geral, a *string* é simplesmente o nome da primitiva na qual a chave será utilizada (*e.g.*, `"RSA"`).

As *strings* de transformação não são sensíveis a caixa, pelo que `"AES"` e `"aes"`, por exemplo, são totalmente equivalentes.

Devido às muitas possíveis combinações, a documentação da JCA provê uma lista exaustiva com todas as possíveis *strings* de transformação para cada classe, bem como especificações quanto aos formatos gerais. Isto pode ser encontrado no Apêndice A do "Java Cryptography Architecture (JCA) Reference Guide" (disponível em https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html) ou, mais recentemente, no "Java Cryptography Architecture Standard Algorithm Name Documentation for JDK 8" (disponível em https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Signature).


## A Classe *Cipher*

Como um primeiro exemplo de *Engine Class*, vamos considerar a classe `Cipher`. Como o nome sugere, esta é a classe utilizada quando deseja-se realizar a cifra ou decifra de determinada informação. Note que tanto cifras simétricas quanto assimétricas utilizam esta mesma classe.

Antes de se realizar cifras ou decifras com um objeto da classe `Cipher`, é necessário fazer uma inicialização do mesmo. Isto é realizado através de alguma versão do método `init()`. Embora existam várias sobrecargas do mesmo, em geral este método deve ser chamado com ao menos dois parâmetros: um modo operacional e uma chave. 

É importante não confundir o modo operacional, no contexto da inicialização de um objeto da classe `Cipher`, com o modo de operação de uma cifra (*e.g.*, ECB, CBC). Aqui, o modo operacional denota simplesmente que tipo de operação desejamos realizar com o objeto `Cipher`: cifra ou decifra. Este modo de operação é passado para o método `init()` por meio de constantes definidas na classe `Cipher`. Por exemplo, a constante `Cipher.ENCRYPT_MODE` denota a operação de cifra, enquanto a `Cipher.DECRYPT_MODE` denota a decifra.

Além dos modos de cifra e decifra, há ainda dois outros modos adicionais chamados *wrap* e *unwrap*. A rigor, estes métodos também realizam, respetivamente, cifra e decifra. Entretanto, eles são disponibilizados especificamente para a cifra / decifra de chaves representadas como objetos da classe `Key`. Embora seja possível cifrar / decifrar chaves utilizando os modos de cifra e decifra, seria necessário fazer algumas manipulações prévias com o objeto `Key` de forma a obter o conteúdo da chave em alguma codificação. Ao contrário, nos modos *wrap* e *unwrap* há métodos que recebem ou retornam diretamente objetos `Key`, o que facilita a programação.

Uma vez inicializado o objeto da classe `Cipher`, este disponibiliza 4 métodos principais para cifra e decifra:

- `update()`: `byte[] → byte[]` – continua a operação incremental.
- `doFinal()`: `byte[] → byte[]` – finaliza a operação.
- `wrap()`: `Key → byte[]` – cifra chave.
- `unwrap()`: `byte[], ... → Key` – decifra chave.

Ambos os métodos `update()` e `doFinal()` são utilizados para cifrar ou decifrar uma mensagem - a depender do modo especificado na inicialização. Ambos recebem como argumento um *array* de bytes que representa o texto plano ou o texto cifrado, a depender do caso. Igualmente, o retorno é um *array* de bytes que representa o texto cifrado (no modo de cifra) ou o texto decifrado (no modo de decifra). 

Mas por que, então, há dois métodos para cifra ou decifra? E quando se deve utilizar cada um deles?

O método `doFinal()` **sempre deve ser chamado para concluir o processo de cifra / decifra**. Se a mensagem inteira a ser cifrada ou decifrada já está disponível, então podemos simplesmente chamar o `doFinal()` uma única passando toda a mensagem como argumento, sem a necessidade de realizar chamadas ao `update()`. 

Um exemplo disto pode ser visto no trecho de código a seguir:

```Java
// Associa a chave key a cifra
cipher.init(Cipher.ENCRYPT_MODE, key);
// Mensagem a ser cifrada
String msg = new String("Mensagem secreta!");
// Mostra bytes da mensagem a ser cifrada
prettyPrint(msg.getBytes());
// Cifra mensagem com chave key
byte[] bytes = cipher.doFinal(msg.getBytes());
```

Neste trecho, assuma que `cipher` é uma instância previamente criada através de uma chamada à função `getInstance()`. Igualmente, suponha que `key` refere-se a um objeto do tipo `SecretKey` previamente gerado. Nestas condições, o trecho começa por inicializar o objeto `cipher`, neste caso habilitando-o para a operação de cifra. Nas linhas seguintes, cria-se uma mensagem em texto plano qualquer que é impressa no ecrã. Como a mensagem é pequena e está integralmente disponível na variável `msg`, basta uma única chamada ao método `doFinal()` que já retorna todo o criptograma resultante.

Igualmente, podemos realizar uma decifra de forma análoga:

```Java
// Decifra com mesma chave da cifra
cipher.init(Cipher.DECRYPT_MODE, key);
byte[] bytes2 = cipher.doFinal(bytes);
// Mostra a mensagem original
System.out.println(new String(bytes2));
```

Neste trecho, inicializa-se um objeto `cipher` previamente instanciado no modo de decifra utilizando-se alguma chave estabelecida anteriormente. Aqui, assumimos que o texto cifrado completo encontra-se disponível na variável `bytes`. Nestas condições, toda a decifra pode ser realizada em uma única chamada ao método `doFinal()`, cujo resultado (texto decifrado) é impresso logo em seguida. Em ambos os trechos, note as conversões entre `String` e `byte[]`, necessárias para uso dos métodos da JCA. 

Embora os trechos de código anteriores funcionem, em certas situações, um programa pode ter apenas acesso gradual a pedaços do texto a ser cifrado ou decifrado. Neste caso, o método `update()` permite que a cifra ou decifra seja feita também de forma gradual: a cada novo pedaço do texto a que temos acesso, fazemos uma chamada ao método `update()`. Cada chamada ao `update()` retorna o próximo pedaço do texto cifrado / decifrado. Porém, mesmo neste modo, é sempre necessário fazermos uma chamada final ao método `doFinal()`, que retornará a porção final do texto cifrado / decifrado.

Considere, como exemplo, o seguinte trecho de código:

```Java
// Obtém linha da entrada padrão in (Scanner) e adiciona
// quebra de linha que é removida pelo nextLine
nl = in.nextLine() + System.lineSeparator();

while (! System.lineSeparator().equals(nl)) {
    // Gera cifra parcial em tmp e concatena no criptograma c
    tmp = cipher.update(nl.getBytes());
    c = concatBytes(c, tmp);

    // Obtém próxima linha da entrada padrão
    nl = in.nextLine() + System.lineSeparator();
}

// Finaliza cifra e concatena bytes finais no criptograma c
tmp = cipher.doFinal();
c = concatBytes(c, tmp);
```

Aqui, o código realiza a leitura, linha por linha, de uma *string* longa provida pelo utilizador através do teclado. O processo termina quando o utilizador prime a tecla `<enter>` em uma linha vazia (o que é sinalizado pela chamada `in.nextLine()` retornar uma *string* vazia). 

Como a mensagem a ser cifrada é disponibilizada gradativamente, há duas possibilidades. Na primeira, poderíamos acumular a mensagem em texto plano em uma variável e, só quando tivéssemos toda a mensagem disponível, chamaríamos o método `doFinal()`. No entanto, podemos evitar este trabalho com a utilização do método `update()`: a cada nova linha lida, realizamos um `update()` sobre o conteúdo daquela linha. Os trechos de texto cifrado retornados pelas sucessivas chamadas ao método `update()` são, então, concatenados na variável `c`.

Note que na penúltima linha do trecho ainda há uma chamada ao método `doFinal()`. Como dito anteriormente, isto é inegociável: esta chamada precisa ser realizada para finalizarmos o processo de cifra e obtermos os últimos bytes do texto cifrado. Porém, note que aqui há algo diferente: o método `doFinal()` é chamado sem nenhum parâmetro. Isto ocorre porque, da forma como este programa foi estruturado, não se sabe *a priori* qual será a última linha da mensagem. Por este motivo, mesmo esta última linha é cifrada através de uma chamada ao método `update()`, pelo que não resta nenhuma parte do texto plano a ser cifrado com o `doFinal()`. Nestes casos, deve-se utilizar o `doFinal()` sem argumentos como ilustrado aqui.
