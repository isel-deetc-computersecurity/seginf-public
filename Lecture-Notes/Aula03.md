[Changelog]: # (v0: versão inicial por Diego Passos)

# Aula 03 - Modos de Operação

Na última aula, discutimos, entre outros tópicos, as cifras simétricas. Em particular, focamos nas cifras de bloco, cujas primitivas operam sobre blocos de tamanho fixo. Como discutido naquela altura, isto impõe uma restrição pouco prática, já que desejamos esquemas criptográficos que possam operar sobre mensagens de tamanho arbitrário. 

Na aula de hoje, veremos como este problema é resolvido através dos **modos de operação**. Começaremos por discutir características desejáveis dos modos de operação. Depois, estudaremos alguns dos principais modos de operação usados na prática. Veremos ainda como usar ferramentas do OpenSSL para realizar algumas tarefas criptográficas básicas.

## Modos de Operação

Modos de operação podem ser entendidos como algoritmos que utilizam repetidamente as funções de cifra ou decifra de uma primitiva criptográfica para garantir propriedades de segurança para mensagens de tamanho arbitrário. Enquanto as cifras de bloco são capazes de operar apenas sobre mensagens de exatamente o tamanho do seu bloco, os modos de operação processam as mensagens de tamanho arbitrário, obtendo pedaços de tamanho do bloco da primitiva que são, então, cifrados ou decifrados e combinados de alguma forma.

Além de permitir a cifra de mensagens de tamanho arbitrário, há uma série de outras propriedades desejáveis para um modo de operação.

Em primeiro lugar, gostaríamos que eventuais **padrões** que aparecem no texto plano não ficassem aparentes no criptograma. Estes padrões são inerentes de textos escritos em línguas humanas (*e.g.*, como repetições, redundâncias), mas também são encontrados em protocolos de comunicação computacional (*e.g.*, como o facto de muitas requisições HTTP começarem pela *string* `GET /...`). Como discutido na aula anterior, este tipo de padrão pode ser mitigado pelo uso de blocos grandes na primitiva, mas alguns padrões envolvem pedaços da mensagem que distam mais de um bloco, mesmo considerando as primitivas atualmente recomendadas. Assim, seria interessante que o modo de operação pudesse prover algum tratamento especial que ajudasse a camuflar ainda mais estes padrões.

Além disso, há a questão da **eficiência**. Se queremos cifrar grandes volumes de dados, precisamos de métodos que sejam computacionalmente eficientes. Como um modo de operação utiliza as funções de cifra e decifra da primitiva aumentadas com outros passos adicionais, ele apresenta naturalmente custo maior que a primitiva em si. Porém, idealmente, este acréscimo de custo não deveria ser significativamente maior.

Outro aspeto relacionado à eficiência é o **tamanho do criptograma gerado**, em relação texto plano original. Em uma primitiva de cifra de bloco, estes tamanhos são iguais (ambos iguais ao tamanho do bloco). No entanto, como um modo de operação precisa adequar mensagens de tamanho arbitrário ao tamanho do bloco da primitiva, é possível que o texto cifrado resultante seja maior que o texto plano original. Embora isso seja inevitável em geral, não é interessante uma disparidade muito grande de tamanhos, dado que o criptograma deverá ser armazenado ou transmitido, consumindo recursos do sistema.

Há, ainda, características que podem ser desejáveis a depender da aplicação específica. Por exemplo, suponha que um serviço de *streaming*, como o Youtube ou o Netflix, deseje armazenar seus vídeos cifrados. Alguns vídeos têm horas de duração, e é possível que um utilizador esteja interessado em começar a assistir em algum ponto pela metade do conteúdo. Neste caso, seria interessante que a cifra utilizada permitisse começar a decifrar o criptograma a partir de qualquer ponto, e não apenas do início. Caso contrário, seria necessário processar a decifra de horas de vídeo inutilmente - um esforço computacional desnecessário. Similarmente, algumas aplicações podem estar interessadas em alterar um pequeno pedaço de uma grande quantidade de informação previamente cifrada: seria mais eficiente pode manipular apenas a porção a ser alterada, ao invés de decifrarmos todo o conteúdo para posteriormente cifrarmos novamente. Em ambos os casos, a característica desejável do modo de operação é a capacidade de **acesso aleatório**.

Outros aplicações particulares podem beneficiar-se de mecanismos de **correção de erros**. Isso porque um criptograma armazenado em um dispositivo de memória ou transmitido por um enlace de comunicação pode sofrer corrupções de diversas naturezas, incluindo a alteração do valor de certos bits, a remoção de certos bits ou a adição de outros bits. Se não tratadas, estas corrupções tendem a ser amplificadas pelo processo de decifra, resultando em uma mensagem $m'$ ainda mais distante da mensagem original $m$.

É preciso que fique claro, no entanto, que todos estes são requisitos desejáveis, e alguns são mais fundamentais que outros. Desta maneira, nem todos os modos de operação respeitarão todos estes requisitos simultaneamente e no mesmo grau.

## Exemplos de Modos de Operação Populares

Há vários modos de operação que são comumente empregados e disponíveis para uso em bibliotecas criptográficas. Aqui, faremos um breve estudo dos principais.

### Modo *Electronic-Codebook* (ECB)

Em criptografia, um *codebook* refere-se a uma tabela ou método de mapeamento de uma *entrada* para um *código*. Para um exemplo simples de *codebook*, podemos relembrar da cifra monoalfabética vista na primeira aula desta UC. A chave era uma tabela que atribuía a cada letra do alfabeto uma outra letra possivelmente diferente. No processo de cifra, percorria-se o texto plano e, a cada letra, procurava-se a entrada correspondente na tabela: a letra associada àquela entrada era colocada na posição correspondente do criptograma. De forma resumida, podemos dizer que a chave funcionava como uma *lookup-table* que contém mapeamentos para a substituição de símbolos do texto plano por símbolos correspondentes no texto cifrado. Esta tabela é justamente o *codebook* da cifra.

Na prática, toda primitiva de cifra de bloco pode ser entendida com a abstração de um *codebook*. Como as funções de cifra e decifra são sempre determinísticas, dada uma chave específica, **um mesmo valor de bloco sempre gerará exatamente o mesmo criptograma correspondente**. Logo, se tivéssemos memória suficiente, poderíamos simplesmente pré-computar o criptograma resultante de cada possível mensagem em texto plano e simplesmente consultar a tabela para cifrar um bloco (ao invés de computar a função $E(.)$). A mesma analogia pode ser empregada no processo de decifra.

O modo ECB baseia-se justamente nesta abstração. Assim como numa cifra monoalfabética em que ciframos um texto realizando a tradução letra a letra com base em um *codebook*, no modo ECB quebramos a mensagem original $m$ em trechos de tamanho igual ao do bloco usado pela primitiva ($m_1$, $m_2$, ..., $m_L$) e cada trecho é mapeado para o seu criptograma correspondente através da função de cifra de bloco. Ao final do processo, os criptogramas correspondentes a cada um dos $L$ trechos são concatenados gerando o texto cifrado completo. Note que a mesma chave $k$ é usada para cifrar cada trecho.

O processo de decifra é análogo: o texto cifrado recebido é dividido em $L$ criptogramas de tamanho correspondente ao tamanho do bloco da primitiva. A cada um destes $L$ criptograma, aplica-se a função $D(k)(.)$, obtendo o trecho de texto plano correspondente. A mensagem original completa é obtida pela concatenação destes trechos.

O modo ECB é provavelmente a solução mais intuitiva para o problema de cifrar mensagens de tamanho arbitrário com primitivas de cifra de bloco. No entanto, há aspetos positivos e negativos nesta abordagem.

Uma primeira característica evidente deste método é que, para uma mesma chave, blocos de texto plano iguais acabam por gerar blocos de texto cifrado também iguais no criptograma final. Ou seja, o ECB não é efetivo em esconder padrões estatísticos que porventura existam no texto plano a distâncias maiores que a de um bloco da primitiva. 

Um exemplo intuitivo de como isso falha em esconder propriedades importantes da mensagem original pode ser visto na cifra de imagens. Computadores digitais armazenam imagens como sequências de pixels. Cada pixel armazena uma intensidade de cor na forma de um ou mais valores numéricos e, em formatos de imagem mais simples, os pixels aparecem no ficheiro na sequência das linhas da imagem. Suponha, por simplicidade, que cada pixel é armazenado como um número de 32 bits e que a primitiva de cifra trabalha com blocos de 128 bits. Portanto, cada bloco corresponderá à informação de 4 pixels. Suponha, ainda, que a figura tem 400 pixels de largura - correspondendo, portanto, a 100 blocos. Ao aplicarmos o ECB a esta figura, os pixels de cada bloco terão seus valores provavelmente alterados. Por exemplo, 4 pixels brancos consecutivos podem ser mapeados para 4 pixels de diferentes cores. No entanto, todo bloco igual será mapeado para uma mesma sequência de 4 pixels. Isso significa, por exemplo, que um fundo branco homogêneo será mapeado para um determinado padrão de cores, mas que se repetirá para todo o fundo da imagem. Além disso, regiões de borda continuarão a se destacar do fundo, embora com cores provavelmente diferentes. Logo, a depender da composição específica da imagem, a versão cifrada ainda permitirá a percepção de detalhes, como contornos e formas.

Por outro lado, no ECB os blocos são cifrados de forma completamente independente. Isso pode ser vantajoso em muitos aspetos. Um destes é o facto de que o ECB acaba por suportar razoavelmente bem o acesso aleatório: se desejamos decifrar uma porção pequena de um grande volume de dados cifrados, basta determinarmos o(s) bloco(s) ao(s) qual(is) esta porção pertence e decifrar apenas estes. Em outras palavras, não há necessidade de decifrar o conteúdo completo.

A independência entre blocos também tem como consequência uma certa resiliência a erros no texto cifrado. Mais concretamente, bits com valores errados no texto cifrado afetam apenas o conteúdo decifrado nos blocos onde estes ocorrem.

Por fim, a independência entre os blocos permite implementações paralelizadas de esquemas criptográficos baseados no ECB. Em outras palavras, supondo a existência de elementos processadores suficientes, vários blocos da mensagem podem ser cifrados/decifrados simultaneamente, reduzindo o tempo total para a computação da cifra/decifra.

### Modo Cipher Block Chaining (CBC)

O modo CBC ataca o principal problema do ECB: o facto de blocos iguais serem mapeados para trechos de texto cifrado iguais, relevando padrões da mensagem original. Para isto, o CBC adota uma abordagem **em cadeia** da cifra dos trechos da mensagem, em que a cifra de um trecho depende do resultado da cifra dos trechos anteriores.

Como no ECB, o CBC também começa por quebrar a mensagem original $m$ em $L$ trechos $m_1$, ..., $m_L$ do tamanho do bloco da primitiva. No entanto, o resultado da cifra do primeiro bloco, $c_1$, é combinado com o texto plano do segundo bloco, $m_2$, através de uma operação XOR bit-a-bit. Ou seja, ao invés de cifrar diretamente $m_2$, o CBC cifra $m_2 \oplus c_1$. O mesmo processo é repetido para todos os demais blocos. Matematicamente:

$$c_i = E(k)(m_i \oplus c_{i-1}), \forall i \in {1, \dots, L}$$

Desta forma, mesmo que um bloco se repita ao longo de uma mensagem, cada uma de suas ocorrências provavelmente será mapeada para um bloco cifrado diferente, por conta da dependência do resultado da cifra do bloco anterior.

Note que na equação, a mesma lógica de combinar o texto plano do bloco atual com o texto cifrado do bloco anterior é repetida para todos os blocos, **incluindo** o primeiro. Porém, o primeiro bloco não tem um antecessor e, portanto, surge a questão: como definir $c_0$ (*i.e.*, o texto cifrado do bloco anterior que não existe).

O CBC lida com isso com a introdução de um parâmetro chamado **vetor de iniciação** (ou IV, do Inglês *Initialization Vector*). O IV é um valor geralmente escolhido aleatoriamente e, neste método, tem o mesmo número de bits de um bloco. No CBC, o IV é usado justamente como o valor do $c_0$ que é combinado com o $m_1$ na cifra do primeiro bloco.

Mas por que razão utilizar o IV? Dito de outra forma: por que não abrimos uma exceção para o primeiro bloco e ciframos apenas $m_1$ diretamente? Isso é feito para que, para uma mesma chave $k$, duas mensagens que tenham um mesmo primeiro bloco de texto plano sejam potencialmente mapeadas para textos cifrados com valores diferentes. Isso ocorrerá, desde que sejam utilizados IVs diferentes para cada mensagem. Esta decisão dificulta ainda mais a identificação de padrões das mensagens originais no texto cifrado.

O processo de decifra no CBC é similar. Basicamente, cada bloco $c_i$ do texto cifrado é decifrado utilizando-se a função $D(k)(.)$ da primitiva e o resultado é combinado (através de um XOR) com o texto cifrado do bloco anterior. No caso do primeiro bloco, o XOR é feito com o mesmo valor de IV utilizado para a cifra. Matematicamente:

$$m'_i = D(k)(c_i) \oplus c_{i-1}, \forall i \in {1, \dots, L},$$

onde $c_0 = IV$.

O CBC tem algumas propriedades interessantes. Em primeiro lugar, está a propriedade de correção: dados uma mesma chave e um mesmo IV, duas mensagens em texto plano iguais sempre resultam em criptogramas idênticos. Por outro lado, mensagens iguais cifradas com a mesma chave, mas IVs diferentes, resultam em criptogramas diferentes. Além disso, dentro de uma mesma mensagem, blocos iguais de texto plano provavelmente resultam em blocos de texto cifrado diferente devido à natureza de encadeamento deste modo de operação.

Esta última caraterística, em particular, faz com que o CBC esconda muito melhor que o ECB as características estatísticas da mensagem original. Em particular, se voltarmos ao exemplo da imagem citado anteriormente, veremos que o efeito do CBC é gerar uma imagem cifrada com características de ruído (pseudo-)aleatório e, portanto, sem padrões facilmente distinguíveis.

Embora o encadeamento do CBC traga vantagens concretas, também há desvantagens em relação ao ECB. Em particular, o encadeamento cria **dependências** de um bloco para os anteriores tanto no processo de cifra quanto no processo de decifra. Isso reduz a capacidade de paralelização das implementações do ECB, potencialmente tornando-o mais lento. Mais importante, esta dependência também resulta na **propagação de erros**/corrupções de bits no criptograma. Em outras palavras, se um ou mais bits do bloco $c_j$ do criptograma forem corrompidos, isso causará erros na decifra deste bloco e do próximo - particularmente, introduzirá erros nas mesmas posições dos bits errados de $c_j$. O resultado final é que o texto plano resultante da decifra conterá mais erros em relação à mensagem original do que no ECB.

Ainda relativamente à tolerância a erros, observe a reordenação de blocos de texto cifrado tem um impacto também muito maior no texto plano resultante da decifra que no caso do ECB. Particularmente, trocar a ordem de dois blocos do criptograma no ECB simplesmente resulta na troca da ordem dos blocos correspondentes no texto plano recuperado. Porém, os blocos decodificados ainda terão seus conteúdos corretos (embora fora de lugar). Já no CBC, a troca de ordem de dois blocos do texto cifrado afetam **o conteúdo de possivelmente 4 blocos decifrados**.

A propriedade de acesso aleatório também é parcialmente afetada. A decifra de um bloco específico no meio de uma grande mensagem cifrada continua relativamente simples: basta decifrar o bloco desejado e realizar o XOR com o texto cifrado do bloco anterior. Porém, se desejarmos alterar algum bloco intermédio de uma grande mensagem já cifrada, precisamos **cifrar novamente o bloco de interesse e todos os blocos posteriores**, devido ao encadeamento utilizado.

Por fim, existem algumas observações importantes sobre o IV. Em primeiro lugar, deve-se observar que o mesmo IV deve ser usado para a cifra e decifra da mensagem. Isso significa que se Bob cifra uma mensagem em modo CBC e a envia para Alice, não basta que Alice conheça a chave $k$: é preciso que ela conheça também o IV utilizado.

Embora isto pareça um desafio adicional, na prática, não é um problema. Note que, ao contrário da chave $k$, o IV não precisa ser secreto, dado que seu objetivo é simplesmente fazer com que mensagens diferentes (ou que comecem com um primeiro bloco igual) gerem textos cifrados distintos. Logo, saber qual é o IV utilizado para uma determinada mensagem não dá, a princípio, uma vantagem significativa para um atacante que pretenda realizar criptoanálise. Assim, Bob pode simplesmente anexar o IV (em texto plano) ao criptograma (matematicamente representado como $IV || c$) e enviar ambos à Alice. Para decodificar o criptograma, Alice separa o IV e aplica o processo de decifra do CBC.

Por outro lado, há, sim, restrições quanto ao uso e a natureza do IV. Por exemplo, para que o IV seja efetivo, idealmente, ele não deve se repetir. Caso contrário, corremos o risco de ter duas mensagens iguais (ou, ao menos, com inícios iguais) cifradas com o mesmo IV, o que pode conferir vantagens à criptoanálise. É claro que, sendo o IV uma sequência de bits de comprimento fixo, há um número limitado de valores diferentes de IV e, ao cifrarmos um número suficientemente grande de mensagens, eventualmente teremos uma repetição. Entretanto, gostaríamos de reduzir ao máximo a probabilidade de ocorrência de repetições sob pena de deixarmos a cifra vulnerável a criptoanálise.

Um exemplo concreto disto ocorreu com o WEP (*Wired Equivalent Privacy*), o primeiro protocolo de segurança adotado pelo WiFi. O WEP utilizava uma primitiva de cifra de fluxo chamada RC4 que também faz uso de um IV, de maneira similar a como o CBC opera. A cada nova trama transmitida, um novo valor de IV era sorteado de forma pseudo-aleatória. O grupo responsável pela definição do *standard* que baseia o WiFi - o chamado IEEE 802.11 - optou por empregar um IV de 24 bits. Infelizmente, com 24 bits há apenas $2^{24} \approx 16$ milhões de IVs possíveis. Embora este número pareça alto, o Paradoxo do Aniversário mostra que há uma probabilidade de mais de 50% do IV se repetir após 5000 pacotes. Pouco tempo após a publicação do *standard*, foi descoberta uma forma de criptoanálise da cifra do WEP viabilizada pela inspeção de mensagens diferentes com um mesmo IV. Na prática, há hoje ferramentas capazes de recuperar a chave WEP de uma rede WiFi com tráfego típico em questão de poucos segundos. Esta vulnerabilidade severa no WEP levou a sua rápida substituição pelo WPA (*Wi-Fi Protected Access*).

Outra boa prática relacionada ao uso de IVs é que eles **não sejam previsíveis**. Embora não haja problemas em transmitir/armazenar os IVs em texto plano **após** seu uso na cifra de uma mensagem, é importante que um atacante não seja capaz de prever quando determinado valor de IV será utilizado. Do contrário, pode usar esta informação para tentar forçar que a próxima mensagem cifrada com o IV em questão seja uma particularmente escolhida pelo atacante - um ataque conhecido como **texto plano escolhido** -, o que ajuda em certas estratégias de criptoanálise.

### Padding

Tanto o ECB quanto o CBC quebram a mensagem original $m$ em $L$ trechos do tamanho de um bloco da cifra. Isto é simples se o tamanho original da mensagem $s$ é múltiplo inteiro do tamanho do bloco da cifra $n$. Ou seja, se $s\ mod\ n = 0$. Mas, na prática, mensagens podem ter tamanhos arbitrários. Assim, o que acontece se $s\ mod\ n \not= 0$?

Considere, por exemplo, a cifra de uma mensagem $m$ de 664 bits utilizando o modo CBC com a primitiva DES. Lembre-se que o DES utiliza blocos de 64 bits. Porém, $664\ mod\ 64 = 24$. Isso significa que serão gerados $\lfloor \frac{664}{64} \rfloor = 10$ blocos completos, mas "sobrarão" 24 bits do final da mensagem. 

Claramente, estes 24 bits precisam ser cifrados com os outros 10 blocos da mensagem - do contrário, o criptograma resultante não será íntegro e, quando decifrado, originará um texto cifrado diferente de $m'$. Por outro lado, trabalhar apenas com blocos completos é uma restrição da primitiva e, portanto, inegociável. Assim, a única solução possível é completarmos os 24 bits finais da mensagem com mais $64 - 24 = 40$ bits adicionais para termos 11 blocos completos.

Esta solução de adicionar mais bits ao final da mensagem para que ela tenha tamanho adequado à cifra é chamada de ***padding***. Porém, a operação de *padding* tem complicadores. O primeiro deles é: quais os valores dos bits adicionados como *padding*?

Uma primeira abordagem seria adicionar tantos bits 0 quanto necessários para completar o último bloco. Igualmente, poderíamos completar os bits faltantes com o valor 1. Outra solução igualmente boa seria completarmos com bits de valor aleatório. Todas estas três abordagens esbarram no mesmo obstáculo: durante o processo de decifra, como sabemos que a mensagem original tinha 664 bits e que os últimos 40 bits são apenas um *padding* artificial que deve ser ignorado? Uma possibilidade seria armazenarmos também o tamanho da mensagem original, de forma que o *padding* possa ser identificado e removido durante a decifra. Porém, isto aumentaria o tamanho do criptograma gerado.

Uma alternativa mais interessante é o método de *padding* incluído no **PKCS# 5** (a sigla PKCS significa *Public Key Cryptography Standards* e denota um conjunto numerado de *standards* publicados pela RSA Security). O *padding* PKCS #5 determina o número X de **bytes** que faltam para completar o último bloco e adiciona X bytes com o valor X.

Para a ideia fique mais concreta, considere novamente o exemplo da mensagem de 664 bits. São necessários 40 bits ou 5 bytes para completar o último bloco. Assim, o *padding* adicionado pelo PKCS #5 seria composto por 5 bytes com valor `0x05`. Para uma mensagem $m$ hipotética, o conteúdo final seria algo como:

```
Mensagem: | 0x36 0x56 ... | ... | 0xB1 ... 0x05 0x05 0x05 0x05 0x05 |
                                           ------------------------
                                              Padding adicionado
```

Mas por qual razão utilizar o número de bytes inseridos como valor dos bytes de *padding* adicionados? A resposta é que isso permite uma distinção fácil entre o *padding* e os bytes originais da mensagem. Ao decifrar uma mensagem e encontrar o conteúdo acima, o entidade que executa a decifra facilmente identificaria os cinco bytes finais com valor `0x05`, e os removeria.

Há, no entanto, um caso especial. Suponha que Alice recebe um criptograma composto de 10 blocos de 64 bits cada. Suponha, ainda, que, ao decifrar a mensagem, Alice encontra a seguinte composição:

```
Mensagem: | 0x36 0x56 ... | ... | 0xB1 ... 0x01 |
```

Note como o último byte da mensagem decifrada tem valor `0x01`. Este último byte é um *padding*, indicando que o último bloco da mensagem original tinha 1 byte a menos que o tamanho do bloco? Ou faz parte da mensagem original e, portanto, não deve ser removido?

O PKCS #5 resolve esta ambiguidade de forma simples: **sempre haverá ao menos um byte de *padding* a ser removido da mensagem**. No exemplo anterior, isto significa que o último byte é, sim, o *padding* e deverá ser removido. Isto ocorre porque, durante o processo de cifra, se o **tamanho da mensagem é exatamente um múltiplo do tamanho do bloco, então deve-se adicionar um novo bloco inteiro de padding ao final da mensagem**. Considerando novamente blocos de 64 bits, isso significa a adição de $64 / 8 = 8$ bytes de *padding*, cada um com o valor `0x08`.

Resumidamente, o **processo de adição** de *padding* no PKCS #5 é feito da seguinte forma:

1. Calcule o número de número $X$ de bytes necessários para que o comprimento da mensagem seja um múltiplo inteiro do tamanho do bloco $n$.
2. Se $X \not= 0$, adicione $X$ bytes ao final da mensagem, cada um deles com o valor $X$.
3. Caso contrário, adicione $n$ bytes ao final da mensagem, cada um com valor $n$.

Em seguida, a mensagem é cifrada. 

Por outro lado, o processo de remoção do *padding*, executado após a decifra do criptograma, tem os seguintes passos:

1. Observe o valor $Y$ do último byte no texto plano resultante da decifra.
2. Remova os $Y$ últimos bytes do texto plano. Todos devem ter valor $Y$.

Pela descrição acima, nota-se que o **PKCS #5 trabalha sobre bytes**, pelo que não é possível aplicar este método caso o tamanho da mensagem ou o tamanho do bloco da primitiva criptográfica utilizada não forem múltiplos inteiros de 1 byte.

Apesar de parecer uma tarefa relativamente trivial, a forma de inclusão do *padding* pode ter consequências inesperadas na segurança da cifra. Por exemplo, em 2002, S. Vaudenay descreveu um ataque que ficou conhecido como o *padding oracle attack*. O ataque explora o processo de remoção do *padding* e, em particular, a capacidade de receptor do criptograma de verificar se o padding está consistente (*i.e.*, se os $Y$ bytes do *padding* têm realmente o valor $Y$). 

No ataque, S. Vaudenay assume que o atacante, de alguma maneira, consegue observar se o receptor considerou o *padding* correto ou não. Por exemplo, a depender do sistema, o recebimento de uma mensagem com *padding* errado pode fazer com que o receptor responda com uma mensagem de erro específica. O que S. Vaudenay mostrou é que, nestas condições, o atacante pode combinar um bloco de texto cifrado que ele deseja compreender com um outro bloco especialmente construído e enviar a mensagem ao receptor. Com base no *feedback* do receptor quando a se a mensagem resultante decodificada tinha um *padding* válido, é possível recuperar gradativamente partes do texto plano correspondente ao bloco de texto cifrado.

Além de ilustrar o potencial impacto negativo na segurança que esquemas de *padding* têm, o ataque proposto por S. Vaudenay também ilustra outros conceitos interessantes de segurança da informação. Um destes é a ideia de **ataque de texto cifrado escolhido**: ou seja, o ato de um atacante construir mensagens cifradas através de um processo especial e enviá-las para uma das entidades legítimas e conseguir, com isso, facilidades na criptoanálise da cifra. O ataque proposto utiliza ainda o conceito de ***side channel attack***: a ideia de observar determinadas propriedades públicas do sistema atacado com o objetivo de inferir informações confidenciais.

### Modos de Operação em *Stream*

Modos como o ECB e o CBC necessitam da aplicação de algum método de *padding* porque dividem a mensagem cifrada em blocos. Cada um destes blocos, possivelmente acrescidos de *padding*, passam, então, pelas funções de cifra ou decifra da primitiva criptográfica.

Há, no entanto, uma classe alternativa de modos de operação: os chamados **modos de operação em *stream***. Em um modo de operação em *stream*, apenas a função $E(.)$ da primitiva computação é necessária, tanto na cifra quanto na decifra. Além disto, a função não é utilizada para cifrar diretamente o texto plano. Ao invés disto, a função $E(.)$ é utilizada como parte de um **gerador de fluxo de chave** (ou *keystream*). O fluxo de chave é simplesmente uma sequência de bits com aparência aleatória que, em seguida, é utilizado para efetivamente cifrar o texto plano ou decifrar o criptograma, a depende do caso. Estes processos de cifra e decifra consistem do simples XOR bit-a-bit entre o fluxo de chave e o texto plano ou criptograma.

Mais concretamente, o processo de cifra em um modo de operação em *stream* funciona da seguinte forma:

1. Escolhe-se um IV, a partir do qual é construído um bloco de bits de tamanho igual ao do bloco da primitiva utilizada. Este bloco é usado como o **estado** inicial da cifra, aqui denominado $I_0$.
2. O estado atual $I_i$ é passado, juntamente com a chave $k$, pela função $E(.)$. Isso resulta em um bloco de bits $ks_i$ que são os próximos bits no fluxo de chave.
3. À medida que novos bits do texto plano são lidos, eles são cifrados através de um XOR com o próximo bit disponível no fluxo de chave.
4. Caso não haja mais bits disponíveis no fluxo de chave, gera-se um próximo estado $I_{i+1}$ a partir do estado atual $I_i$ e volta-se ao passo 2.

O processo de decifra é análogo:

1. Parte-se do mesmo IV utilizado durante a cifra da mensagem original. A partir dele, é construído um bloco de bits de tamanho igual ao do bloco da primitiva utilizada. Este bloco é usado como o **estado** inicial da decifra, aqui denominado $I_0$.
2. O estado atual $I_i$ é passado, juntamente com a chave $k$, pela função $E(.)$. Isso resulta em um bloco de bits $ks_i$ que são os próximos bits no fluxo de chave.
3. À medida que novos bits do criptograma são lidos, eles são decifrados através de um XOR com o próximo bit disponível no fluxo de chave.
4. Caso não haja mais bits disponíveis no fluxo de chave, gera-se um próximo estado $I_{i+1}$ a partir do estado atual $I_i$ e volta-se ao passo 2.

Nota-se que há um aspeto dos algoritmos aqui descritos que não está bem definido: a geração do estado $I_{i+1}$ a partir do estado atual $I_i$. Isto deve-se ao facto de que há múltiplos modos de operação baseados em *stream* que se diferenciam principalmente por este aspeto. Genericamente, há três abordagens para esta geração do próximo estado:

1. O próximo estado ($I_{i+1}$) é composto pelos bits de texto cifrado ($c_i$) gerados no estado atual ($I_i$). Esta é a estratégia adotada pelo modo *Cipher FeedBack* (CFB).
2. O próximo estado ($I_{i+1}$) é composto pelos bits do fluxo de cifra ($ks_i$) gerados no estado atual ($I_i$). Esta é a estratégia adotada pelo modo *Output FeedBack* (OFB).
3. O próximo estado ($I_{i+1}$) é computado pela aplicação de uma função determinística ($f(.)$) sobre o estado atual ($I_i$). Esta é a estratégia adotada pelo modo *Counter* (CTR).

Os modos de operação em *stream* não necessitam de *padding*: se o tamanho da mensagem não é múltiplo inteiro do tamanho de bloco da primitiva, simplesmente não são utilizados todos os bits do último bloco do fluxo de cifra computado como parte do processo. Isso evita vários inconvenientes discutidos anteriormente, como a geração de um criptograma maior que a mensagem original e os possíveis ataques baseados em vulnerabilidades introduzidas pelo processo de *padding*.

Além disto, os processos de cifra e decifra são idênticos com exceção da entrada: para a cifra a entrada é o texto plano, enquanto para a decifra a entrada é o criptograma. Isso simplifica a implementação e, devido à menor complexidade, torna mais viável uma implementação em *hardware* especializado.

Um ponto crítico para a segurança dos modos de operação em *stream* é a imprevisibilidade do fluxo de chave. Como a cifra em si é simplesmente um XOR bit-a-bit entre o fluxo e o texto plano, repetições de longos trechos do fluxo de chave introduzem correlações entre os textos cifrado e plano. Mais especificamente, observe que se determinados blocos do fluxo de cifra $ks_i$ e $ks_j$ são iguais, então $m_i \oplus m_j = c_i \oplus c_j$ (lembre-se que $c_i = m_i \oplus ks_i$ e, portanto, $ks_i = m_i \oplus c_i$).

Assim, para a segurança deste modo, é essencial que o fluxo de chave seja **aparentemente aleatório** para um atacante que desconhece a chave. Isso requer o uso de uma boa função de cifra $E(.)$, além de cuidados na escolha do IV (principalmente, que este nunca ou raramente se repita).

#### Modo *Counter* (CTR)

O modo *counter* é um exemplo clássico de modo de operação em *stream*. Como o nome sugere, o modo é baseado no conceito de contador. Nele, o estado inicial é $I_0 = IV$. Sempre que o estado necessita ser atualizado, a atualização dá-se por um simples incremente: $I_{i+1} = I_i + 1$. Daí o nome: o estado funciona como um contador incrementado de uma em uma unidade.

Como descrito anteriormente no contexto genérico de modos de operação em *stream*, o estado atual é passado, juntamente com a chave $k$, pela função de cifra $E(.)$ da primitiva escolhida, o que gera o próximo bloco de bits do fluxo de chave. Ao mesmo tempo, os bits da mensagem em texto plano são cifrados um a um através de uma operação XOR com os bits do fluxo de chave. O processo de decifra é idêntico, apenas substituindo-se a entrada: ao invés da mensagem original em texto plano m, faz-se o XOR entre o fluxo de chave e o criptograma.

Em certa altura, o uso de um simples incremento como função de avanço do estado chegou a causar desconfiança sobre a segurança do CTR. Porém, hoje o CTR é amplamente considerado seguro, desde que seja utilizado de acordo com as melhores práticas, nomeadamente: evitar repetições do IV e utilizar boas primitivas criptográficas para a função $E(.)$.

A observação sobre o IV é importante porque, dada uma mesma chave de sessão e um mesmo IV, duas mensagens iguals (ou de mesmo prefixo) resultam em dois criptogramas iguais (ou de mesmo prefixo). Assim, se durante a mesma sessão o IV se repete, corre-se o risco de o atacante obter conhecimento sobre características do texto plano.

Outra característica do CTR - e, mais geralmente, de alguns dos modos de operação em *stream* - é que erros em bits do criptograma não se propagam para outros bits do texto plano. Isso porque cada bit do criptograma corresponde apenas a um bit do texto plano e o fluxo de chave é gerado independentemente do criptograma.

Também deve-se destacar que o CTR permite acesso aleatório de forma relativamente simples. Por exemplo, se os blocos têm 8 bytes e queremos decifrar uma parte da informação que começa no byte da posição 802, podemos simplesmente iniciar o processo de decifra com o estado $I = IV + \lfloor 802 / 8 \rfloor = IV + 100$, ignorar os dois primeiros bytes do fluxo de chaves e começar a decifra pelo byte 802 do criptograma.