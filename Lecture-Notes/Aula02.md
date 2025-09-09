# Aula 02 - Hierarquia de Métodos Criptográficos e Primitivas Simétricas

Nesta aula, discutiremos a principal classificação dos métodos criptográficos: métodos simétricos *vs.* métodos assimétricos. Também discutiremos brevemente o uso de métodos criptográficos para garantir propriedades de segurança da informação que vão além da confidencialidade. Além disso, introduziremos os conceitos de **primitiva criptográfica**, **esquema criptográfico** e **protocolo criptográfico**. Por fim, discutiremos cifras simétricas e alguns exemplos de primitivas para este fim.

## Introdução

Na aula anterior, discutimos brevemente o conceito de ataque, listando alguns tipos de ataques clássicos. Naquela altura, os ataques foram introduzidos em um contexto de comunicação em rede. De fato, a comunicação entre dois dispositivos computacionais constitui uma oportunidade para que um atacante realize alguma ação maliciosa, dado que, com acesso à rede de comunicação, este pode eventualmente ler e modificar mensagens transmitidas.

Porém, a comunicação entre dois dispositivos não é o único momento ou contexto em que informação pode ser atacada. Mesmo no âmbito de um único dispositivo computacional, um processo malicioso - como um *malware* - pode tentar acessar ficheiros com conteúdos sensíveis ou o espaço de endereçamento de outro processo para ler dados sigilosos.

Em ambas as circunstâncias, a linha de defesa mais básica é a mesma: a criptografia. Mais especificamente, mecanismos criptográficos são utilizados para cifrar pedaços de informação sigilosos com o objetivo de garantir confidencialidade. Além disso, se aplicada de maneira correta, é possível usar criptografia também para garantir a integridade e/ou a autenticidade de pedaços de informação.

## Mecanismos Criptográficos

No contexto de segurança informática, métodos criptográficos podem ser discutidos em vários níveis de abstração. 

Ao nível mais baixo, encontram-se as **primitivas criptográficas**. Trata-se de algoritmos ou da definição de operações matemáticas básicas utilizadas no processo de cifra / decifra de um mecanismo criptográfico. Embora estes algoritmos possam ser usados por si só para cifrar e decifrar mensagens, eles comummente apresentam limitações fortes que impedem sua aplicação geral - a menos que sejam combinados com outros métodos ou processos auxiliares.

Para um exemplo mais concreto destas limitações, consideremos dois exemplos de primitivas criptográficas populares: o DES (*Data Encryption Standard*) e o AES (*Advanced Encryption Standard*). Ambas pertencem a uma classe de primitivas criptográficas chamadas de **cifras de bloco**. As cifras de bloco caracterizam-se por operar em blocos de bits de tamanho fixo. Por exemplo, o AES opera sobre blocos de 128 bits, enquanto o DES opera sobre blocos de 64 bits. Assim, se desejamos cifrar uma mensagem de **exatamente** 128 bits, a primitiva AES é perfeitamente adequada. Mas e se a mensagem tiver mais que 128 bit? E se tiver menos que 128 bits? Em ambos os casos, a resposta é que são necessários métodos adicionais para adequar a aplicação do AES - ou de qualquer que seja a primitiva em questão - à cifra da mensagem.

Quando consideramos a primitiva criptográfica aumentada com estes métodos adicionais de processamento da mensagem, obtemos o que se denomina um **esquema criptográfico**. É importante notar que um esquema criptográfico também está associado a uma **tarefa criptográfica**. A tarefa criptográfica nada mais é que o objetivo que se deseja alcançar com o emprego do esquema criptográfico. Por exemplo, uma tarefa criptográfica comum - e que tem sido discutida até aqui nesta UC - é a cifra: a aplicação da criptografia com o objetivo de garantir a confidencialidade de uma informação. Porém, como estudaremos nas próximas aulas, existe a tarefa criptográfica denominada de **assinatura digital**, que tem por objetivo garantir integridade e autenticidade. 

Em resumo: **é possível utilizar uma mesma primitiva criptográfica (*e.g.*, AES) com diferentes métodos auxiliares para alcançar esquemas criptográficos voltados a diferentes tarefas criptográficas.**

Esquemas criptográficos geralmente são identificados por um sistema de nomeação que identifica a primitiva criptográfica e outros métodos auxiliares. Um exemplo é o `DES-CBC-PKCS5Padding`: utiliza-se o DES como primitiva criptográfica através de um **modo de operação** CBC e um mecanismo de ***padding*** chamado PKCS5Padding (por ora, não é importante percebermos exatamente o que é um modo de operação ou *padding*; estes conceitos serão estudados posteriormente). Outro exemplo, a título de ilustração, seria o `RSA-OAEP-MGF1-SHA1​`.

> [!NOTE]
> Ilustração de um exemplo mais concreto de esquema criptográfico e do seu uso.
> - Aceder a uma página qualquer (*e.g.*, https://www.google.com/) que utilize HTTPS e mostrar a especificação do esquema criptográfico usado no certificado digital.
>   - No Google Chrome: `Cadeado > Connection is Secure > Certificate is valid > Details > Certificate Signature Algorithm`.


Há ainda o que convencionou-se chamar de **protocolos criptográficos**. Protocolos criptográficos utilizam um ou mais esquemas criptográficos e, sobre estes, definem sequências de ações a serem realizadas pelas entidades envolvidas na aplicação, de forma a garantir as propriedades de segurança desejadas. Por exemplo, mais à frente nesta UC, estudaremos um protocolo criptográfico de comunicação chamado TLS (*Transport Layer Security*). No início de uma comunicação TLS, utiliza-se um esquema criptográfico baseado em um tipo particular de primitiva criptográfica chamada de **criptografia de chave assimétrica**. Passada esta fase inicial, o TLS passa a empregar esquemas criptográficos baseados em outra classe de primitivas denominada de **criptografia de chave simétrica**. Embora o TLS possa trabalhar com diferentes combinações de esquemas criptográficos, um exemplo é o `    TLS_RSA_WITH_DES_CBC_SHA​`, que utiliza a primitiva criptográfica de chave assimétrica RSA seguida do DES como primitiva de chave simétrica.

## Introdução à Criptografia Computacional

Há muitas formas diferentes de se classificar as primitivas criptográficas modernas. Mas talvez a classificação mais básica e amplamente utilizada é a entre primitivas **simétricas** e **assimétricas** - estas últimas, também denominadas primitivas de **chave pública**. A diferença fundamental entre as primitivas simétricas e assimétricas está nas chaves utilizadas nestes métodos: nas primitivas de chave simétrica, há apenas uma chave utilizada tanto no processo de cifra quanto no processo de decifra; nas primitivas assimétricas, são usadas chaves diferentes para cada uma destas duas operações.

Primitivas simétricas e assimétricas possuem vantagens e desvantagens que serão discutidas em maior detalhe posteriormente. Elas também se diferenciam por serem adequadas a cenários / situações ligeiramente diferentes. No entanto, tanto as primitivas simétricas quanto as assimétricas podem ser usadas para os mesmos tipos de tarefas criptográficas, nomeadamente a garantia de confidencialidade e de integridade / autenticidade. 

Há, portanto, esquemas criptográficos simétricos e assimétricos tanto para confidencialidade, quanto para integridade / autenticidade. Um esquema simétrico que visa a confidencialidade é genericamente denominado uma **cifra simétrica**. Analogamente, um esquema assimétrico que visa a confidencialidade é uma **cifra assimétrica**. Já um esquema simétrico que objetiva a integridade / autenticidade é denominado **MAC** (*Message Authentication Code*), enquanto no caso simétrico tem-se a **Assinatura Digital**.

## Criptografia Simétrica

Como explicado anteriormente, a característica definitiva da criptografia simétrica é o uso de uma mesma chave para as tarefas de cifra e decifra. Em linhas gerais, primitivas de criptografia simétrica tendem a ser computacionalmente mais leves que suas contrapartes assimétricas, sendo, portanto, preferíveis quando há grandes volumes de dados a serem cifrados.

Neste sentido, um uso comum da criptografia simétrica é como **cifra de sessão**, *i.e.*, para cifrar/decifrar dados durante uma sessão de comunicação entre duas ou mais partes. No início da sessão, as partes envolvidas devem, de alguma forma, entrar em acordo numa **chave de sessão**: uma chave simétrica partilhada (*i.e.*, conhecida por todas as partes) a ser utilizada exclusivamente naquela sessão de comunicação. As chaves de sessão são, portanto, efêmeras: são criadas para uma sessão específica, usadas por relativamente pouco tempo (*i.e.*, o tempo de uma sessão) e depois descartadas.

Não obstante sua típica vantagem em termos de complexidade computacional, a criptografia simétrica tem entre suas desvantagens justamente o processo de **estabelecimento** desta chave simétrica partilhada. Como a mesma chave deve ser conhecida por todas as partes legitimamente envolvidas na comunicação, mas **não por terceiras partes não autorizadas**, o processo de estabelecimento de chaves é normalmente desafiador. Não se pode, por exemplo, transmitir a chave em texto plano entre as partes envolvidas na comunicação, porque um atacante poderia intercetar estas mensagens.

De facto, esquemas simétricos em geral assumem uma de duas soluções:

1. O estabelecimento da chave dá-se por comunicação **fora-de-banda** (*i.e.*, por algum outro meio de comunicação seguro entre as partes). Um exemplo seria a hipótese de que as partes envolvidas podem se encontrar antes da comunicação e trocar pessoalmente as informações relativas à chave.
2. O estabelecimento da chave simétrica dá-se com o auxílio de um esquema criptográfico assimétrico. Esta solução é adotada, por exemplo, pelo TLS.

Mais à frente, nesta UC, quando estudarmos os detalhes do TLS, discutiremos de maneira mais concreta o problema de estabelecimento de uma chave simétrica de sessão.

### Cifra Simétrica

Se ignorarmos por ora o problema de estabelecimento da chave de sessão e assumirmos que todas as partes envolvidas conhecem a chave partilhada, o uso geral das cifras simétricas se torna bastante simples: 

1. Quando uma das partes deseja enviar uma mensagem com confidencialidade para as demais, aplica-se a função de cifra utilizando-se a chave de sessão, resultando no criptograma.
2. O criptograma é, então, enviado através do canal de comunicação inseguro (*e.g.*, uma rede de comunicação, uma *pen* USB). 
3. Do lado receptor, utiliza-se o mesmo esquema criptográfico com a mesma chave para decifrar a mensagem e obter o texto plano original.

Formalmente, um esquema de cifra simétrica é definido por três componentes:

- Uma função $G(.)$ de geração de chave. Em geral, chaves para cifra simétrica são geradas por funções aleatórias para que o atacante não possa fazer suposições sobre o valor da chave.
- A função $E(.)$ de cifra. Esta função mapeia um par `(chave, mensagem)` para um criptograma. Esta função deve suportar como entrada mensagens com comprimentos arbitrários. Em outras palavras, qualquer sequência arbitrária de bits pode ser passada como entrada da função $E(.)$. Matematicamente, podemos escrever que a mensagem $m$ em texto plano pertence ao domínio $`\{0,1\}^*`$.
- A função $D(.)$ de decifra. Esta função mapeia um par `(chave, criptograma)` para uma mensagem em texto plano. Esta função deve suportar como entrada criptogramas com comprimentos arbitrários. Em outras palavras, qualquer sequência arbitrária de bits pode ser passada como entrada da função $D(.)$. Matematicamente, podemos dizer que o criptograma $c$ pertence ao domínio $`\{0,1\}^*`$.

Dadas estas definições, podemos reescrever o processo de uso de um esquema de cifra simétrica da seguinte forma:

1. Através da função $G(.)$, gera-se uma chave (aleatória) $k$. 
2. Quando uma das partes deseja enviar uma mensagem $m$ qualquer, calcula-se o criptograma $c = E(k)(m)$. Apenas o criptograma é enviado pelo canal inseguro.
3. Ao receber o criptograma $c$, o receptor calcula $m' = D(k)(c)$.

Se ambos os lados utilizaram a mesma chave e **se a mensagem não sofreu alterações** durante a transmissão, $m' = m$, tornando a comunicação bem-sucedida.

### Propriedades de um Esquema de Cifra Simétrica

Independentemente da primitiva criptográfica utilizada, há diversas propriedades comuns a qualquer esquema de cifra simétrica.

Uma destas propriedades é a de **correção**. Uma cifra simétrica é considerada correta se, e somente se:

$$\forall m \in \lbrace 0,1\rbrace^*, \forall k \in Keys: D(k)(E(k)(m)) = m,$$
onde $Keys$ denota o conjunto de todas as chaves que podem ser geradas pela função de geração de chaves $G(.)$. Em termos menos formais, isso significa que, para qualquer que seja a mensagem original $m$ e a chave escolhida $k$, ao cifrar $m$ e depois decifrar o criptograma resultante obtemos a mensagem original $m$ novamente. Dito de outra forma: se o resultado da decifra é uma mensagem $m' \not= m$, então ou o criptograma não corresponde à mensagem em texto plano $m$ ou chaves diferentes foram utilizadas nas funções de cifra e de decifra.

Outra propriedade de uma cifra simétrica é o seu nível de **segurança**. Uma cifra simétrica é considerada segura quando é **computacionalmente infazível** obter $m$ a partir de $c$ sem o conhecimento da chave $k$. Lembre-se da última aula que é sempre possível *tentar* um ataque de força bruta: o atacante gera potenciais chaves, computa a função de decifra utilizando estas chaves e verifica se alguma resulta em uma mensagem em texto plano que faça sentido. No entanto, se o número de chaves possíveis for suficientemente grande, ataques de força bruta são simplesmente infazíveis, porque testar todas as chaves possíveis demoraria tempo demasiado e adivinhar a chave correta em poucas tentativas tem baixíssima probabilidade de ocorrer.

Deve-se considerar também que os ataque de força bruta não são as únicas possibilidades de criptoanálise de uma cifra simétrica. Conforme os exemplos vistos na última aula mostram, análises estatísticas podem auxiliar a reduzir significativamente o espaço de busca pela chave $k$, eventualmente tornando obter a mensagem $m$ a partir do criptograma $c$ fazível. Todos estes aspetos devem ser avaliados para analisar a segurança de uma cifra simétrica.

Outra propriedade comum a toda cifra simétrica - já que é, por definição, o que caracteriza as primitivas simétricas - é o uso de uma mesma chave $k$ para os processos de cifra e decifra. Veremos mais tarde que há outros tipos de cifra - nomeadamente as assimétricas ou de chave pública - que não possuem esta mesma propriedade.

Além disso, uma cifra simétrica deve suportar cifrar mensagens de tamanho arbitrário. Ou seja, tanto a mensagem em texto plano $m$ quanto seu criptograma correspondente são simplesmente sequências de bits que, portanto, podem ter qualquer dimensão.

Por outro lado, há também propriedades que **não** são contempladas pelas cifras simétricas. Uma delas é a garantia da integridade: apenas através do uso de uma cifra simétrica, não é possível a um receptor determinar se a versão da mensagem recebida é íntegra. 

#### Exemplificação das Propriedades 

Para compreendermos mais concretamente estas propriedades, considere um cenário em que Bob e Alice comunicam-se através de um canal inseguro. Assuma que, neste canal inseguro, a atacante, Eva, é capaz capturar, ler, modificar mensagens, além de ser capaz de enviar mensagens passando-se por outras identidades. Como forma de proteção, Alice e Bob utilizam uma cifra simétrica: antes de enviar uma mensagem $m$ a Alice, Bob computa $c = E(k)(m)$, para uma chave $k$ qualquer (assuma que apenas Bob e Alice conhecem $k$), e envia o criptograma, ao invés da mensagem em texto plano.

Como, por hipótese, Eva desconhece a chave $k$, assumindo que a cifra simétrica utilizada é **segura**, é computacionalmente infazível que Eva consiga obter $m$ a partir do criptograma capturado. No entanto, nada impede Eva de realizar alterações - mesmo que aleatórias - ao criptograma $c$ e de repassar esta versão modificada à Alice. 

Seja $c'$ o novo criptograma após as alterações feitas por Eva. Ao recebê-lo, Alice computa $E(k)(c') = m'$, utilizando a mesma chave $k$ usada por Bob (pois a cifra é **simétrica**). Se a cifra simétrica utilizada é **correta**, então $m' \not= m$ (porque $c' \not= c$). 

Lembre-se, no entanto, que Alice não conhece a mensagem original, pelo que, em geral, não tem como saber que $m'$ não foi precisamente a mensagem originalmente enviada por Bob. Desta forma, Alice **não pode verificar a integridade** da mensagem e, portanto, corre o risco de aceitar ou processar uma mensagem não íntegra.

Analogamente, num ataque do tipo *spoofing*, Eva pode enviar um suposto criptograma composto por qualquer sequência aleatória de bytes à Alice, passando-se por Bob. Seja este criptograma aleatório $c''$. Ao recebê-lo, Alice computa $E(k)(c'') = m''$. Mesmo não havendo qualquer relação entre $c''$ e algum criptograma legítimo criado por Bob, Alice não dispõe de qualquer mecanismo que permita identificar que se trata de uma mensagem **não autêntica**.

### Primitivas de Cifra Simétrica

A primitiva de cifra simétrica está no cerne de um esquema de cifra simétrica. Felizmente ou infelizmente, há uma gama considerável de opções. Felizmente porque, como engenheiros, podemos optar pela primitiva que mais se adequa às características do sistema informático que estamos a desenvolver. Por outro lado, nem todas as primitivas são igualmente seguras, o que impõe um ônus adicional ao engenheiro relativamente à escolha de uma boa primitiva.

Uma forma de escolhermos primitivas seguras é optarmos por aquelas que correspondem a ***standards* internacionais** ou que são descritas em publicações acadêmicas reconhecidas. Por exemplo, o **NIST** (*National Institute of Standards and Technology*), uma agência governamental dos Estados Unidos que se ocupa da regulação tecnológica, mantém *standards* relativos à criptografia que definem as primitivas criptográficas e outros métodos associados recomendados para uso não-militar pelo governo daquele país. 

Um exemplo de primitiva criptográfica que já foi um *standard* do NIST é o **DES** (*Data Encryption Standard*), aprovado como padrão em 1977. O DES se manteve como *standard* do NIST por cerca de 25 anos, até ser substituído pelo **AES** ao final de 2001. Por sua vez, após mais de 20 anos, o AES continua como o atual *standard* do NIST para criptografia simétrica. Até o momento da escrita deste documento, não são conhecidas criptoanálises computacionalmente fazíveis a esta primitiva.

Os *standards* do NIST são, em geral, baseados em algoritmos e métodos desenvolvidos por terceiros. Na prática, durante o processo de padronização, é comum que o NIST avalie uma série de algoritmos em uma espécie de competição até que se determine o ideal para receber a chancela de *standard*. Por exemplo, a primitiva AES corresponde a uma versão ligeiramente modificada de um método chamado Rijndael, publicado por Joan Daemen e Vincent Rijmen 3 anos antes.

Também é importante notar que mesmo algoritmos outrora considerados seguros podem se tornar **obsoletos** e, portanto, **não recomendados**. É o caso do DES que, embora tenha sido um *standard* por mais de duas décadas, tem hoje utilização não mais recomendada (em grande parte, devido ao uso de chaves relativamente pequenas de 56 bits, o que pode viabilizar ataques de força bruta). Nota-se, portanto, a necessidade não só de uma escolha apropriada da primitiva criptográfica no momento do projeto de um sistema informático, mas também de um desenho de sistema que permita a fácil **substituição** desta primitiva ao longo do ciclo de vida do produto, em caso de obsolescência.

Além de os *standards* oferecerem primitivas criptográficas de reconhecida segurança, outro aspeto positivo é a facto de que estas primitivas são **publicamente conhecidas**. Isto significa que as especificações destas primitivas estão disponíveis para, entre outras coisas, análise pela comunidade acadêmica e pela indústria, de forma que possíveis vulnerabilidades tendem a ser detetadas e reportadas antes que possam ser exploradas na prática.

A abordagem inversa, do uso de primitivas proprietárias cuja especificação não é publicamente conhecida, não é recomendada. Conhecida como **segurança por obscuridade**, esta filosofia pode parecer fornecer camadas adicionais de segurança (já que um potencial atacante supostamente não conhece sequer como a primitiva criptográfica funciona), mas tende a resultar em soluções com vulnerabilidades devido ao menor escrutínio público. Historicamente, há diversos exemplos de soluções baseadas em obscuridade que eventualmente foram quebradas, destacadamente em tecnologias antigas de telefonia móvel.

Além da escolha da primitiva em si, há a questão da implementação a ser utilizada. Devido à complexidade das primitivas criptográficas modernas e a criticidade do papel que estas desempenham, não é recomendado que cada desenvolvedor codifique a sua própria implementação. Igualmente, não se deve confiar em qualquer implementação de métodos criptográficos. Ao contrário, uma **boa prática** importante é o uso de **implementações bem consolidadas** e de confiança.

### Primitivas de Cifra de Bloco

Tanto o DES quanto o AES são primitivas da classe das **cifras de bloco**. Relembre que numa cifra de bloco as funções de cifra e decifra operam sobre blocos de informação de tamanho fixo. Por exemplo, o AES trabalha com blocos de 128 bits, enquanto o DES trabalha com blocos de 64 bits.

Um exemplo mais simples (porém bem menos seguro) de cifra de bloco segue o seguinte modelo:

- Função de geração de chaves $G = k$: gera chaves que correspondem a números inteiros positivos de 64 bits.
- Função de cifra $E(k)(m) = m \oplus k = c$. Aqui, assume-se que a mensagem é representada como um número de 64 bits e o operador $\oplus$ denota a operação XOR bit-a-bit.
- Função de decifra $D(k)(c) = c \oplus k = m'$. Igualmente, aqui, assume-se que o criptograma é representado como um número de 64 bits.

Enquanto a cifra acima descrita utiliza apenas a operação de XOR bit-a-bit, cifras simétricas de aplicação prática como o DES e o AES utilizam operações mais sofisticadas. O DES, por exemplo, utiliza, entre outras, as seguintes operações:

- **Divisão**: o bloco de 64 bits é dividido em duas metades para as operações seguintes.
- **Expansão**: cada semi-bloco de 32 bits é expandido para 48 bits através de duplicação de 16 dos seus bits.
- **Substituição**: pedaços de 6 bits do bloco são substituídos de acordo com uma tabela fixa de mapeamento.

Estas e outras operações são organizadas em **rodadas** (ou *rounds*): a cada rodada, executa-se uma sequência destas operações de forma que a entrada de uma rodada é a saída da rodada subsequente. No DES, são utilizadas 16 rodadas.

Mais formalmente, uma primitiva de cifra de bloco define suas funções de cifra e decifra, $E(.)$ e $D(.)$, sobre o domínio das mensagens com **exatamente** $n$ bits, onde $n$ denota o tamanho do bloco da primitiva. Matematicamente, isso significa que $`m, c \in \{0, 1\}^n`$ para serem processados por $E(.)$ e $D(.)$. 

Esta restrição é bastante inconveniente, dado que gostaríamos que os **esquemas** de cifra simétrica fossem capazes de operar sobre mensagens (e criptogramas) de comprimento arbitrário. A solução para isto está em dotar os esquemas de cifra simétrica de mecanismos para dividir a mensagem original $m$ em um ou mais blocos, mesmo que o comprimento de $m$ não seja múltiplo do tamanho do bloco da primitiva. Estudaremos isto em mais detalhes na próxima aula.

Importa também destacar que o tamanho do bloco não necessariamente corresponde ao tamanho da chave utilizada pela primitiva criptográfica. Por exemplo, embora o AES trabalhe com blocos de 128 bits, suas chaves podem ter 128, 192 ou 256 bits. 

De forma geral, os **tamanhos do bloco e da chave têm relação direta com a segurança do método**. Chaves compostas por **muitos bits dificultam os ataques por força bruta**, já que cada bit adicional dobra o número de possíveis chaves, reduzindo a probabilidade de um atacante acertar casualmente a chave em poucas tentativas e aumentando o tempo necessário para testar todas as chaves no pior caso. 

Já o tamanho do bloco ajuda a **esconder características estatísticas** do texto. Lembre-se da aula anterior que vimos como a frequência das letras em um criptograma gerado por uma cifra monoalfabética pode ser usada na criptoanálise. Entre outros motivos, esta cifra é susceptível a este tipo de ataque porque usa blocos de uma única letra. Ao aumentarmos o tamanho do bloco para conter várias letras, mascaramos as características estatísticas de cada letra individual, conferindo maior segurança. Uma ressalva é que blocos muito grandes são inconvenientes quando desejamos cifrar informações pequenas: mais à frente, veremos que se a mensagem em texto plano é menor que o tamanho do bloco, o criptograma se torna também maior, necessitando de mais recursos de armazenamento ou transmissão de dados.

Por fim, cabe a observação de que a classe das cifras de bloco não é a única existente. Ao contrário, as cifras de bloco opõem-se às chamadas **cifras de fluxo**. As funções de cifra e decifra em uma cifra de fluxo utilizam uma abordagem bastante diferente, sendo capazes de operar sobre mensagens de tamanho efetivamente arbitrário e, portanto, sem a necessidade de mecanismos adicionais. Embora as cifras de fluxo sejam particularmente interessantes para determinadas aplicações, como a cifra de dados transmitidos por certos tipos de *links* de comunicação, para os propósitos desta UC teremos foco particular nas cifras de bloco.
