# Aula 06 - Esquemas Assimétricos

Nas últimas aulas, temos discutido exclusivamente esquemas e primitivas criptográficas simétricas. Porém, como visto anteriormente, há também esquemas e primitivas análogos **assimétricos**. Na aula de hoje, começaremos a estudá-los.

## Uma Breve Revisão

Lembre-se que os esquemas criptográficos podem ser classificados tanto em relação aos seus objetivos quanto em relação ao tipo de primitiva criptográfica utilizada. 

Relativamente aos objetivos, há esquemas que visam garantir confidencialidade, enquanto outros objetivam garantir a integridade / autenticidade. Vimos ainda exemplos como o GCM, que fornece simultaneamente confidencialidade e integridade / autenticidade.

Independentemente do objetivo a ser alcançado, os esquemas criptográficos podem ser simétricos ou assimétricos. Em um esquema simétrico, uma mesma chave $k$ deve ser partilhada por todas as partes legítimas da comunicação e utilizada tanto nas operações de proteção (*e.g.*, cifra) quanto nas operações de desproteção (*e.g.*, decifra). Por outro lado, nos **esquemas assimétricos** há **chaves diferentes** empregadas em cada fase: uma chave é usada na proteção, enquanto a outra é usada na desproteção.

Tanto no universo simétrico quanto no assimétrico, esquemas que visam garantir a confidencialidade são denominados **cifras**: cifras simétricas ou cifras assimétricas, a depender do caso. Já estudamos também os esquemas MAC, denominação dada a esquemas simétricos para garantia da integridade / autenticidade. Por outro lado, esquemas assimétricos que objetivam garantir integridade / autenticidade são denominados **assinaturas digitais**.

Nesta aula, estudaremos propriedades gerais dos esquemas assimétricos e veremos em mais detalhes o funcionamento das cifras assimétricas. As assinaturas digitais serão tópico da próxima aula.

## Esquemas Assimétricos: Chaves Públicas e Privadas

Um esquema assimétrico baseia-se em uma **primitiva assimétrica**. Ao contrário do que ocorre nas primitivas simétricas, nas assimétricas as funções $E(.)$ e $D(.)$ devem receber chaves distintas para que a decifra desfaça o que a cifra fez. Tem-se, portanto, **duas chaves diferentes** utilizadas para uma mesma comunicação, mas para **propósitos distintos**.

As primitivas e esquemas assimétricos são, também, frequentemente denominados (primitivas ou esquemas) **de chave pública**. Esta nomenclatura sugere que na criptografia assimétrica uma das chaves pode ser **publicada**. Isto contrasta totalmente com a criptografia simétrica, na qual a segurança é completamente dependente da chave ser conhecida apenas pelas partes legítimas.

De facto, como o nome sugere, a criptografia assimétrica geralmente envolve uma **chave pública** e uma **chave privada**, de modo que o que uma chave faz (*e.g.*, cifrar) e outra desfaz (*e.g.*, decifrar). Como os nomes sugerem, a **chave pública pode ser conhecida por todos**, inclusive por eventuais atacantes. Mesmo assim, as propriedades de segurança são plenamente garantidas. Já a chave privada deve ser mantida em segredo, sendo conhecida apenas por **uma entidade**.

Este último ponto também é muito importante: mesmo se queiramos utilizar criptografia assimétrica para uma comunicação entre Alice e Bob, **apenas Alice deve conhecer sua própria chave privada**. Nem mesmo Bob, uma outra parte legítima da comunicação, deve ter acesso à chave privada de Alice. 

Por outro lado, Bob deve ter acesso à chave pública de Alice. Mas isto não é um grande obstáculo, dado que esta chave não é secreta. Assim, teoricamente, Alice poderia simplesmente enviar sua chave pública em texto plano para Bob. Aliás, por ser uma chave pública, Alice pode divulgá-la amplamente (*e.g.*, publicar em um anúncio de jornal, colocá-la na descrição pública das suas redes sociais).

Um exemplo concreto de divulgação de chaves públicas é o protocolo criptográfico PGP (*Pretty Good Privacy*). Entre outras aplicações, o PGP é popularmente adotado para a transmissão segura de *e-mails*. Embora os detalhes do PGP não sejam relevantes neste ponto da unidade curricular, ele utiliza tanto esquemas simétricos quanto assimétricos. Para os assimétricos, cada utilizador deve ter um **par de chaves pública e privada**. A chave pública deve ser informada a todos que porventura desejem enviar um *e-mail* seguro ao utilizador. Uma forma comum de divulgação é o utilizador emissor do *e-mail* incluir sua chave pública PGP na assinatura da mensagem do *e-mail* enviado. Deste modo, qualquer utilizador receptor deste *e-mail* passa a conhecer a chave pública do emissor.

## Chaves Públicas e Privadas: Usos

Repare que tanto a chave pública $k_{pub}$ quanto a chave privada $k_{priv}$ podem ser utilizadas em ambas as funções $E(.)$ e $D(.)$ de uma primitiva simétrica. Ou seja, dada uma mensagem original $m$, é matematicamente possível calcularmos tanto $c = E(k_{priv})(m)$ quanto $c' = E(k_{pub})(m)$. Porém, repare que, em geral, $c \not= c'$. Por outro lado, um criptograma computado com a chave pública pode ser decifrado com a chave privada: $D(k_{priv})(E(k_{pub})(m)) = m$. Analogamente, um criptograma cifrado com a chave privada pode ser decifrado com a chave pública: $D(k_{pub})(E(k_{priv})(m)) = m$.

Por outro lado, um criptograma computado a partir da chave pública **não** é decifrável pela mesma chave pública: $D(k_{pub})(E(k_{pub})(m)) \not= m$. Igualmente, um criptograma computado a partir da chave privada **não** é decifrável pela mesma chave privada: $D(k_{priv})(E(k_{priv})(m)) \not= m$.

Dadas estas características e relações entre as chaves, a pergunta natural é: quando utilizar cada uma das chaves em um esquema assimétrico? A resposta para isto depende da tarefa criptográfica a ser realizada e em ambos os casos devemos nos perguntar **qual é a operação privada?**.

Para cifras assimétricas, a operação privada é a **decifra**. Ou seja, gostaríamos de permitir que qualquer entidade fosse capaz de enviar uma mensagem cifrada para Alice, mas que apenas Alice fosse capaz de decifrá-la. Note que o inverso faz pouco sentido no contexto da confidencialidade: como a chave pública é, por definição, pública e o que a chave privada cifra a pública decifra, se Alice enviar uma mensagem por um canal inseguro cifrada com a sua chave privada qualquer um será capaz de decifrá-la. 

Agora, consideremos o objetivo de garantir a integridade / autenticidade. Neste caso, a operação privada é gerar a marca de autenticidade da mensagem: apenas Alice deve ser capaz de gerá-la para as suas mensagens. Por outro lado, a verificação pode ser permitida a todos. Assim, qualquer que seja o processo de geração da marca, este deve envolver a chave privada. Do outro lado, a verificação deve depender apenas da chave pública.

## Usos da Criptografia Assimétrica

Por motivos que ficarão mais claros mais a frente, a criptografia assimétrica raramente é utilizada para cifrar grandes volumes de dados. Ao invés disso, a criptografia assimétrica é aplicada em situações específicas em que o uso de criptografia simétrica não é suficiente ou não é desejável.

Um caso de uso particularmente relevante é a viabilização do estabelecimento seguro de chaves simétricas. Como estudamos anteriormente, estabelecer uma chave simétrica através de um canal inseguro não é trivial, porque não podemos simplesmente enviá-la em texto plano. Por outro lado, na criptografia assimétrica uma das chaves é pública e, portanto, seu estabelecimento se torna bem mais simples. Uma vez estabelecido um par de chaves pública e privada, podemos utilizar um esquema de cifra assimétrico para cifrar uma chave simétrica de sessão. Depois disto, ambas as partes conhecerão de forma segura a chave de sessão e poderão utilizar um esquema simétrico para o resto da comunicação.

Outro uso bastante popular da criptografia assimétrica é na forma da assinatura digital. Assim como no caso das cifras, o estabelecimento da chave compartilhada necessária a um esquema MAC é difícil. Já no caso da assinatura digital, por se tratar de um esquema assimétrico, este estabelecimento é mais simples (embora haja, ainda, alguns complicadores que estudaremos mais tarde). Além da maior facilidade de estabelecimento de chave, outra vantagem da assinatura digital em relação ao MAC é que ela é capaz de fornecer a propriedade de **não-repúdio**. Isso porque a assinatura digital só pode ser gerada pela entidade que tem acesso à chave privada que, por definição, é única. Já no MAC, a marca é gerada a partir de uma chave compartilhada conhecida por todas as entidades legítimas envolvidas na comunicação.

## Cifras Assimétricas: Visão Geral

Uma vez compreendidas as relações entre as chaves e as propriedades gerais da criptografia assimétrica, não é difícil perceber como as cifras assimétricas operam.

Considere um cenário de comunicação entre Bob e Alice. Em particular, neste momento, assuma que estejamos interessados em garantir a confidencialidade das mensagens enviadas **de Bob para Alice**. Para tanto, o processo começa pela **geração de chaves**: **Alice** deve gerar um par de chaves pública e privada. A chave privada será mantida em segredo por Alice, enquanto **a chave pública será informada à Bob.**

Uma vez estabelecido o par de chaves, Bob pode proceder para a cifra das mensagens. Basicamente, Bob irá aplicar a função $E(.)$ de cifra da primitiva à mensagem $m$ utilizando a **chave pública de Alice**, obtendo um criptograma $c = E(k_e)(m)$, onde $k_e = k_{pub}$. Este criptograma será, então, enviado pelo canal inseguro e recebido por Alice.

Por sua vez, Alice irá decifrar a mensagem aplicando a função $D(.)$ de decifra da primitiva ao criptograma $c$ utilizando **sua chave privada**. O texto plano obtido será, portanto, $m' = D(k_d)(m)$, onde $k_e = k_{priv}$. 

Assumindo que as chaves corretas foram utilizadas e que o criptograma não foi alterado de nenhuma maneira, $m' = m$.

## Esquemas de Cifra Assimétrica

Mais formalmente, um esquema de cifra assimétrica é definido pelas mesmas três funções que definem um esquema simétrico: $(G, E, D)$. No entanto, estas funções apresentam especificidades para o caso assimétrico.

Uma destas especificidades está na função $G(.)$ de geração de chaves. Isso porque esta função gera não apenas uma, mas um par de chaves. O contra-domínio da função $G$, portanto, é um conjunto $KeyPairs \subseteq PublicKeys \times PrivateKeys$, onde $PublicKeys$ e $PrivateKeys$ denotam, respetivamente, os conjuntos das possíveis chaves públicas e privadas.

Como no caso simétrico, a função $G(.)$ deve ser probabilística, de forma que sua execução gere o par de chaves de forma imprevisível. Do contrário, um atacante poderia facilmente deduzir o par de chaves e, em particular, a chave privada.

Repare, no entanto, que nem todo par $(k_{pub}, k_{priv})$, com $k_{pub} \in PublicKeys, k_{priv} \in PrivateKeys$, corresponde a um par de chaves válido. Em outras palavras: para que obtermos a propriedade desejada de que o que a chave pública cifra a chave privada decifra (e vice-versa), estas chaves precisam preservar algum tipo de relação específica. Logo, nem todo par arbitrário de chaves é viável. Desta forma, apesar de probabilística, a função $G(.)$ deve garantir a geração de pares de chaves corretamente relacionadas.

Outra especificidade dos esquemas de cifra assimétrica está no domínio das funções de cifra e decifra, $E(.)$ e $D(.)$. Os exemplos de primitivas simétricas estudados nesta UC eram, de forma geral, primitivas de bloco. Logo, suas funções de cifra e decifra operavam sobre mensagens de texto plano e criptogramas de um tamanho fixo de bits - o tamanho do bloco. Já as primitivas assimétricas, em geral, trabalham com textos planos e criptogramas representados como **números inteiros positivos** menores que um determinado valor limite.

Desta maneira, para cifrar uma mensagem $M$, originalmente na forma de uma *string* de bits, é necessário primeiro um processo de **codificação** de $M$ para a forma de um número inteiro. Um dos desafios desta codificação é garantir que a mesma seja inversível (de forma que, ao decifrar um criptograma, seja possível decodificá-lo de volta para a mensagem original sem ambiguidade). Considere, por exemplo, duas mensagens diferentes `M1 = 01` e `M2 = 001`. Se simplesmente interpretarmos ambas as mensagens como números inteiros em binário, com o bit de maior peso sendo o mais à esquerda, obtemos a mesma representação inteira para ambas: $m1 = m2 = 2$. 

Logo, é necessária alguma função de codificação mais sofisticada para transformar a *string* de bits original em um número inteiro de forma não ambígua. Na prática, as funções de codificação utilizadas acabam por permitir o mapeamento de qualquer *string* de tamanho **menor que um determinado limite** para valores inteiros do domínio da função de cifra.

## Propriedades dos Esquemas de Cifra Assimétrica

Um esquema de cifra assimétrica é considerado **correto** se, e somente se:

$$
\forall m \in M, \forall (k_e, k_d) \in KeyPairs, D(k_d)(E(k_e)(m)) = m
$$

Na equação, $M$ denota o domínio das mensagens suportadas pela função de cifra $D(.)$, enquanto $k_e$ e $k_d$ denotam as chaves - neste contexto, respetivamente, as chaves pública e privada. Sob estas condições, a equação significa que, para qualquer mensagem de texto plano possível e par válido de chaves, o esquema consegue decifrar corretamente o criptograma gerado pela função de cifra. Deve-se observar que, na equação, a função de cifra utiliza a chave $k_e$, enquanto a de decifra utiliza a chave $k_d$.

Para que um esquema de cifra assimétrica seja considerado seguro, é preciso que seja **computacionalmente infazível** obter $m$ a partir de $c = E(k_e)(m)$ sem o conhecimento da chave $k_d$. Destaque-se que esta propriedade deve ser válida **ainda que o atacante conheça a chave $k_e$**, dado que, em geral, $k_e$ será pública. Repare que isto também implica ser computacionalmente infazível obter $k_d$ a partir de $k_e$.

Assim como ocorre com as cifras simétricas, uma cifra assimétrica não é, a princípio, autenticada. Logo, a simples aplicação da cifra sobre uma mensagem não garante a integridade. Como exemplo, suponha que Bob cifra uma mensagem $m$ com a chave pública de Alice, obtendo o criptograma $c = E(k_{pub})(m)$. Agora suponha que Eva intercepte a transmissão e altere aleatoriamente determinados bits de c, obtendo $c' \not= c$. Ao receber $c'$, Alice aplica a função de decifra obtendo $m' = D(k_{priv})(c')$. Em geral, $m' \not= m$, mas como não conhece $m$, Alice não tem qualquer mecanismo que a permita detetar a falta de integridade da mensagem recebida.

Outra característica comum dos esquemas de cifra assimétricos é a sua alta complexidade computacional - ao menos, em relação aos esquemas simétricos. Embora a complexidade varie de esquema para esquema, de forma geral, esquemas simétricos são bem mais leves. Na prática, é comum haver esquemas assimétricos terem tempos de execução **duas ou mais ordens de grandeza** maiores que os simétricos. Isto acaba por desencorajar a aplicação generalizada dos esquemas assimétricos que, em geral, ficam limitados a casos de uso particulares (embora de grande importância).

Uma outra consequência da maior complexidade computacional dos esquemas simétricos é a limitação (prática) dos tamanhos das mensagens por eles cifradas. Enquanto cifras simétricas trabalham com mensagens no domínio $\{0, 1\}^*$, as cifras assimétricas comumente estão limitadas a mensagens de uma determinada dimensão máxima. Como já discutido, as cifras simétricas utilizam codificações que mapeiam mensagens de dimensão menor que um determinado limite de bits para valores inteiros positivos, de modo a adequá-las à função de cifra. A princípio, mensagens maiores poderiam ser quebradas em blocos de tamanho menores que o limite da cifra e cifrados de acordo com algum modo de operação, como acontece com as cifras simétricas. Porém, a aplicação sucessiva das funções de cifra e decifra a vários blocos teria custo computacional muito elevado, o que torna esta opção incomum na prática.

Estas últimas características fazem com que o uso mais comum das cifras assimétrica seja em **esquemas híbridos**. Num esquema híbrido, uma das partes legítimas da comunicação gera uma chave **simétrica** de sessão e a cifra com um esquema assimétrico. Como chaves simétricas são relativamente pequenas (*e.g.*, 64 bits, 128 bits, 256 bits), esta cifra tipicamente é realizada com uma única chamada à função $E(.)$ da primitiva assimétrica, o que tem um custo computacional aceitável. Depois disso, a chave simétrica cifrada é enviada a outra parte legítima que a decifra. Desta forma, tem-se o estabelecimento seguro da chave de sessão que, em seguida, é utilizada para cifrar os dados da comunicação em si.

## Princípios da Primitiva RSA

O RSA é a mais popular primitiva assimétrica existente. O nome RSA faz referência aos seus proponentes: Ron Rivest, Adi Shamir e Leonard Adleman. Esta primitiva foi desenvolvida na década de 1970 e até hoje tem sido amplamente utilizada com em esquemas assimétricos.

A função $G(.)$ geradora de chaves no RSA retorna um par de chaves pública $(E, N)$ e privada $(D, N)$. Repare, portanto, que cada chave é, na verdade, um par de valores. Mais especificamente, $E$, $D$ e $N$ são números inteiros. Note, ainda, que o valor $N$ é parte de ambas as chaves, pública e privada.

Para cifrar uma mensagem $M$, uma entidade, de posse da chave pública $(E, N)$ computa:

$$
C = M^E\ mod\ N
$$

Em outras palavras, a mensagem M é codificada como um número inteiro positivo. Este número é elevado à $E$, o **expoente da chave pública**. No entanto, esta exponenciação é calculada em módulo $N$, o **módulo da chave pública**. Isso resulta num número $C$ que é o texto cifrado.

A decifra da mensagem é feita de forma muito similar, mas com a chave privada:

$$
M = C^D\ mod\ N
$$

Neste caso, o criptograma - na sua forma numérica - é elevado a $D$, o **expoente da chave privada** em módulo $N$, o **módulo da chave privada**. O resultado é a mensagem em texto plano $M$ na sua forma numérica.

Como as operações de cifra e decifra são realizadas módulo $N$, então duas mensagens $M$ e $M'$ tal que $M \equiv M'\ (mod\ N)$ resultariam no mesmo criptograma $C$. Por exemplo, se $N = 15$, $M = 4$ e $M' = 19$ resultariam em exatamente no mesmo texto cifrado. Logo, para evitar ambiguidades ao decifrarmos mensagens, é preciso a restrição de que $M < N$. Esta restrição acaba por limitar o comprimento das mensagens cifradas com o RSA, como citado anteriormente.

Embora, como em qualquer aplicação de criptografia, a função $G(.)$ deva ser probabilística, os valores $E$, $D$ e $N$ não podem ser totalmente arbitrários. Do contrário, as funções de cifra e decifra não funcionarão como esperado. Em particular, é preciso que:

$$
\forall M < N, (M^E\ mod\ N)^D\ mod\ N = M
$$

Ou, de forma mais simplificada:

$$
\forall M < N, M^{ED} mod\ N = M
$$

Não é difícil encontrar exemplos de valores $N$, $E$ e $D$ para os quais esta relação não seja respeitada. Por exemplo, considere $N = 15, E = 2, D = 3$ e $M = 5$. Ao cifrarmos a mensagem encontramos:

$$
C = 5^2\ mod\ 15 = 10
$$

Já no processo de decifra, teríamos:

$$
M' = 10^3\ mod\ 15 = 10
$$

Claramente, $M' \not= M$. Portanto, a função $G(.)$ precisa, de alguma forma, gerar um par de chaves aleatórias que, no entanto, garantam a correção da cifra.

Os detalhes do processo de geração de chaves no RSA vão além do escopo desta UC, porque envolvem alguma matemática avançada. Porém, iremos ainda assim discutir superficialmente algumas características deste processo.

O processo começa pela escolha **aleatória** de dois números primos **distintos e grandes** $P$ e $Q$. Em geral, é mais seguro selecionar valores relativamente distantes para $P$ e $Q$. A partir deste ponto, o processo de geração de chaves se torna **determinístico**. Dados $P$ e $Q$, calcula-se $N = P\cdot Q$. Perceba, portanto, que $N$ é aleatório, mas tem propriedades específicas, nomeadamente ser o produto de dois números primos. 

Para aplicações modernas do RSA, $N$ será um número tipicamente entre $2^{1023}$ e $2^{4095}$. Trata-se, então, de um número muito grande. Embora o método funcione para valores menores de $N$, quanto menor $N$ mais susceptível a ataques de força bruta o RSA se torna.

Uma vez estabelecido o valor $N$, o processo de geração de chaves prossegue para as escolhas de $E$ e $D$. Neste ponto, poderíamos nos perguntar: se $N$ é divulgado como parte da chave pública, e $E$ e $D$ são escolhidos a partir de $N$, então o que impede um atacante de simplesmente reproduzir o processo de geração de chaves deste ponto em diante?

A resposta para isto está na forma pela qual $E$ e $D$ são selecionados. Apesar de ser teoricamente possível realizar esta escolha por força bruta (*i.e.*, testar todos os pares possíveis de $E$ e $D$ com todas as possíveis mensagens $M < N$ e verificar se $M^{ED} mod\ N = M$), esta abordagem é computacionalmente infazível porque $N$ é muito grande. Ou seja, há muitas possíveis combinações de $E$, $D$ e $N$ a verificar.

Ao invés disto, $E$ e $D$ podem ser encontrados através de um algoritmo de complexidade bem mais baixa. Este algoritmo começa por calcular o valor $\lambda(N)$, onde $\lambda(.)$ denota a *Função de Carmichael*. Esta é uma função da área matemática de Teoria dos Números que, para o caso geral, é difícil calcular para números grandes. Porém, há duas propriedades particulares desta função que podem facilitar seu cálculo:

- Se $N$ é um número composto e pode ser escrito como $N = P\cdot Q$, então $\lambda(N) = mmc(\lambda(P), \lambda(Q))$, onde $mmc(.)$ denota o menor múltiplo comum entre seus argumentos.
- Se $P$ é um número primo, então $\lambda(P) = P - 1$.

Note que a forma pela qual $N$ é selecionado - *i.e.*, como um produto de dois primos - faz com que $\lambda(N)$ possa ser calculado facilmente. Em particular:

$$
\lambda(N) = mmc(P - 1, Q - 1)
$$

Computacionalmente, calcular o menor múltiplo comum entre dois números é relativamente simples, mesmo para números grandes. Assim, durante o processo de geração das chaves por uma entidade legítima, é rápido calcular $\lambda(N)$. A partir deste valor, derivar $E$ e $D$ também é computacionalmente simples, com base em alguns teoremas bem conhecidos.

Por outro lado, considere o perspetiva do atacante. Ele conhece $N$, **mas não conhece $Q$ ou $P$**. Por este motivo, ao contrário da entidade legítima, o atacante não consegue calcular prontamente $\lambda(N) = mmc(P - 1, Q - 1)$. 

Teoricamente, o atacante poderia tentar fatorar $N$ nos seus fatores primos $P$ e $Q$. No entanto, o problema de fatoração de números inteiros grandes ainda é computacionalmente complexo. Para computadores tradicionais, não são conhecidos algoritmos com complexidade polinomial de fatoração. Na década de 1990, Peter Shor mostrou a existência de um algoritmo polinomial de fatoração para **computadores quânticos**. Mais recentemente, foram demonstradas execuções do Algoritmo de Shor em computadores quânticos reais. No entanto, estas execuções sempre foram realizadas sobre números pequenos (por exemplo, 15) e ainda hoje há obstáculos consideráveis para a aplicação do Algoritmo de Shor para números da ordem de grandeza dos valores de $N$ utilizados no RSA. 

Na prática, dado um $N$ suficientemente grande, é computacionalmente infazível um atacante derivar $E$ e $D$, garantindo o segredo da chave privada.


> [!NOTE]
> Ilustração de geração de chaves RSA com o OpenSSL.
>    - Gerar uma chave privada de 2048 bits para Alice:
>
>    ```
>    # openssl genrsa -out alice_private_key.pem 2048
>    ```
>
>    - Observar o conteúdo do ficheiro:
>
>    ```
>    # cat alice_private_key.pem 
>    -----BEGIN PRIVATE KEY-----
>    MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC5vg+4LMQF7ITF
>    wqtWEJdqC1QjZ5Y7ldklltAYzP/onXf/S/jhLqE28EM93AlDWlE/gWWZAqaI//Pd
>    jp2ByePdCI4RLSuwnCar3J9x2+UdxYVkBcM9/doStwlD84y20TmsQ1lxEP1Kx2ao
>    OAANklZIJ78RZluve72yWvULgfmEwaOeCNNx1+M4eOZDzH8S81na9j7ddI/6JGze
>    e3iVk6BwbDUVfPG7AqtbwmX2lujbahPiOvPKvXh+w9biMlAcV/og+XrLP3CHPlPE
>    VuUVflxLO084WY2PUcRoWwytPVMIC5TulJSGAnVyMNuCO7ooiAL8lEq2j2IBLbkI
>    lxrBrUy1AgMBAAECggEAC0GfwECY7JGl24+YRJH4Kx6z+E/5xHNTV+TTiFKRcQPC
>    AZXAEnLPUFNahTAwMNsTW27jElUdHzursrycLQES2b1VUWh/DP1EQVwQdEeoPe1P
>    en1NlL8tQmnB76fzL3fEhVJbju+W+hDVgXz1+9ySIAil7/mtCsMhVDgQ66PyftSx
>    lcQ7+dp5MJft1HJmDB+CSACxqtinRbjI9Hzr14BRiqxzU3iBqRgZseCMvZt09FFF
>    ApNJFr0ITGpDyX40XQdWMHucjc+bYjYWcMDUY0U43DN7tHi1GnFmNqoq9PZ15NX8
>    oN8Mjql7aByufzHBtckJS+jgGEOobIP1tFCigXk4rQKBgQDYcxhBhFKRNdFaiOJg
>    l0WweTGWAUuTCWAIKqDxWznuy2ytZEUrxQHXxxUWh0MUzIwHRUYawRPOT7E28KZ/
>    CUxA0p2z2KjocT1GYWCygBdnTQMhmOKuk4rVGxzETredqEGAXOjUVlaqNcqFltnl
>    YvGXmYsTq7gjXVGrutHANDP/UwKBgQDbrpJZ407EDRw6Vxl7+dk1Tj1M8/0FYbD9
>    xPFNmRdYB2cBHK2BZ6fTWItzxzvFdmRFC0l6vHqb7n5ylOwwQv/UC7Omiqx0rLzW
>    M1UXDeAKfofEQsVMkkszRx3oK42f2Rq1ArSePHlR6XA+ghu1Y7dD/GN5s/KjKN+K
>    OXH63uDq1wKBgB8vJdV6nPKy0EC86/CCmtW8ADreYOcEOMO0cI+VVxliUXwBsD2I
>    GqXd1tnyDuYWOi5p+pmsK5BTxvJlZXdz/XpxXaslkeA7QFq9eNL+xWBqpgLXJgGE
>    3EGrsE9QEAA014sI17qP3diT+2OXwjjcMnZm9rSUzui5byTQu5t3ae5lAoGAKipI
>    tu4VOTYW+++p2YzsLvoUdAfEwdqtYRgDBNTkkSXilGkPuG3P3ZX6Nj/Abjc6KbJv
>    6RnyIqnVJWMGLoE9n1KbqdSmI0Lgf/CcXW9xWDJPFtGGExtlSbNVvzFU3qCKKsIZ
>    LuIGmz1GKQlKDjfUfj0cTsjGQqDevyYlhXRwEUMCgYEAr7LshSH3kl0ERAVaqA3K
>    jsvoaNB3YwrV3w1fANnQCAYZXV+MVMrAhEhKm+WvB1h80OM47ga1t67jK6SL4xGL
>    TGlTUC3FC8FFEIL3QN+/Mh5Nk7xfGDhfhXkNApaFC86QxrUEg2sCyTbwHWqDyCSr
>    HfGbmXvD7rVXp6dzyt5Nzss=
>    -----END PRIVATE KEY-----
>    ```
>
>    - Observar o conteúdo descodificado do ficheiro:
>
>    ```
>    # openssl rsa -in alice_private_key.pem -text
>    Private-Key: (2048 bit, 2 primes)
>    modulus:
>    00:b9:be:0f:b8:2c:c4:05:ec:84:c5:c2:ab:56:10:
>    97:6a:0b:54:23:67:96:3b:95:d9:25:96:d0:18:cc:
>    ff:e8:9d:77:ff:4b:f8:e1:2e:a1:36:f0:43:3d:dc:
>    09:43:5a:51:3f:81:65:99:02:a6:88:ff:f3:dd:8e:
>    9d:81:c9:e3:dd:08:8e:11:2d:2b:b0:9c:26:ab:dc:
>    9f:71:db:e5:1d:c5:85:64:05:c3:3d:fd:da:12:b7:
>    09:43:f3:8c:b6:d1:39:ac:43:59:71:10:fd:4a:c7:
>    66:a8:38:00:0d:92:56:48:27:bf:11:66:5b:af:7b:
>    bd:b2:5a:f5:0b:81:f9:84:c1:a3:9e:08:d3:71:d7:
>    e3:38:78:e6:43:cc:7f:12:f3:59:da:f6:3e:dd:74:
>    8f:fa:24:6c:de:7b:78:95:93:a0:70:6c:35:15:7c:
>    f1:bb:02:ab:5b:c2:65:f6:96:e8:db:6a:13:e2:3a:
>    f3:ca:bd:78:7e:c3:d6:e2:32:50:1c:57:fa:20:f9:
>    7a:cb:3f:70:87:3e:53:c4:56:e5:15:7e:5c:4b:3b:
>    4f:38:59:8d:8f:51:c4:68:5b:0c:ad:3d:53:08:0b:
>    94:ee:94:94:86:02:75:72:30:db:82:3b:ba:28:88:
>    02:fc:94:4a:b6:8f:62:01:2d:b9:08:97:1a:c1:ad:
>    4c:b5
>    publicExponent: 65537 (0x10001)
>    privateExponent:
>    0b:41:9f:c0:40:98:ec:91:a5:db:8f:98:44:91:f8:
>    ...
>    ```
>
>    - Gerar a chave pública a partir da chave privada:
>
>    ```
>    # openssl rsa -in alice_private_key.pem -out alice_public_key.pem -pubout
>    ```
>
>    - Exibir conteúdo (codificado) do ficheiro:
>
>    ```
>    # cat alice_public_key.pem
>    -----BEGIN PUBLIC KEY-----
>    MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAub4PuCzEBeyExcKrVhCX
>    agtUI2eWO5XZJZbQGMz/6J13/0v44S6hNvBDPdwJQ1pRP4FlmQKmiP/z3Y6dgcnj
>    3QiOES0rsJwmq9yfcdvlHcWFZAXDPf3aErcJQ/OMttE5rENZcRD9SsdmqDgADZJW
>    SCe/EWZbr3u9slr1C4H5hMGjngjTcdfjOHjmQ8x/EvNZ2vY+3XSP+iRs3nt4lZOg
>    cGw1FXzxuwKrW8Jl9pbo22oT4jrzyr14fsPW4jJQHFf6IPl6yz9whz5TxFblFX5c
>    SztPOFmNj1HEaFsMrT1TCAuU7pSUhgJ1cjDbgju6KIgC/JRKto9iAS25CJcawa1M
>    tQIDAQAB
>    -----END PUBLIC KEY-----
>    ```
>
>    - Exibir as informações descodificadas da chave:
>
>    ```
>    # openssl rsa -pubin -in alice_public_key.pem -text
>    Public-Key: (2048 bit)
>    Modulus:
>        00:b9:be:0f:b8:2c:c4:05:ec:84:c5:c2:ab:56:10:
>        97:6a:0b:54:23:67:96:3b:95:d9:25:96:d0:18:cc:
>        ff:e8:9d:77:ff:4b:f8:e1:2e:a1:36:f0:43:3d:dc:
>        09:43:5a:51:3f:81:65:99:02:a6:88:ff:f3:dd:8e:
>        9d:81:c9:e3:dd:08:8e:11:2d:2b:b0:9c:26:ab:dc:
>        9f:71:db:e5:1d:c5:85:64:05:c3:3d:fd:da:12:b7:
>        09:43:f3:8c:b6:d1:39:ac:43:59:71:10:fd:4a:c7:
>        66:a8:38:00:0d:92:56:48:27:bf:11:66:5b:af:7b:
>        bd:b2:5a:f5:0b:81:f9:84:c1:a3:9e:08:d3:71:d7:
>        e3:38:78:e6:43:cc:7f:12:f3:59:da:f6:3e:dd:74:
>        8f:fa:24:6c:de:7b:78:95:93:a0:70:6c:35:15:7c:
>        f1:bb:02:ab:5b:c2:65:f6:96:e8:db:6a:13:e2:3a:
>        f3:ca:bd:78:7e:c3:d6:e2:32:50:1c:57:fa:20:f9:
>        7a:cb:3f:70:87:3e:53:c4:56:e5:15:7e:5c:4b:3b:
>        4f:38:59:8d:8f:51:c4:68:5b:0c:ad:3d:53:08:0b:
>        94:ee:94:94:86:02:75:72:30:db:82:3b:ba:28:88:
>        02:fc:94:4a:b6:8f:62:01:2d:b9:08:97:1a:c1:ad:
>        4c:b5
>    Exponent: 65537 (0x10001)
>    ...
>    ```
>


## Exemplo de Aplicação do RSA

A título de ilustração, vamos ver um exemplo numérico do uso do RSA para cifrar e decifrar uma mensagem. Para este exemplo, utilizaremos os seguintes valores para as chaves: $N = 33, E = 7$ e $D = 3$. Logo, a chave pública é $(7, 33)$, enquanto a chave privada é $(3, 33)$.

Digamos que se deseje cifrar a mensagem $M = 8$. Para isto, utilizamos a chave pública:

$$
C = 8^7\ mod\ 33 = 8\cdot 64^3\ mod\ 33 = 8\cdot 31^3\ mod\ 33 = 8\cdot 25\ mod\ 33 = 2 
$$

Por outro lado, para decifrar o texto cifrado $C = 2$, utilizamos a chave privada:

$$
M = 2^3\ mod\ 33 = 8\ mod\ 33 = 8
$$

## Esquema Híbrido

Apesar de termos utilizado valores pequenos para o exemplo didático anterior, na prática, o RSA opera sobre valores muito grandes (*e.g.*, números de 2048 bits ou da ordem de $10^{616}$). Devido à necessidade de realizar operações aritméticas com valores bastante elevados, os processos de cifra e decifra do RSA são lentos.

Por este motivo, o uso típico do RSA como cifra assimétrica é como parte de um esquema híbrido simétrico/assimétrico. Neste esquema, uma das partes legítimas da comunicação, digamos Alice, gera um par de chaves RSA publica $K_e$ e privada $K_d$. A chave privada é mantida em segredo por Alice, enquanto a pública é enviada para a outra parte da comunicação, Bob. 

Como a chave pública não necessita de confidencialidade, seu envio pode, a princípio, ser feito em texto plano. Em aulas posteriores, veremos que este envio da chave pública não é tão trivial assim e apresenta alguns pequenos desafios. Mas, por ora, basta notarmos que se trata de um processo significativamente mais simples que o estabelecimento seguro de uma chave simétrica.

De posse da chave pública de Alice, Bob gera uma **chave simétrica de sessão** $k$. A chave $k$ é, então, cifrada com a chave pública de Alice: $c_k = E(k_e)(k)$. Em seguida, Bob envia o criptograma resultante a Alice, que o decifra com a sua chave privada.

Neste ponto, Alice e Bob compartilham uma mesma chave simétrica $k$ que pode ser utilizada, por exemplo, para um esquema de cifra simétrica. A partir deste momento, as demais mensagens da comunicação são cifradas/decifradas com a chave simétrica de forma a reduzir o esforço computacional.

Na prática, portanto, os esquemas híbridos utilizam a cifra assimétrica como uma forma segura de estabelecer a chave simétrica.

## Cifra Assimétrica: Arquitetura Interna

Ao longo desta aula, citamos múltiplas vezes que primitivas assimétricas normalmente operam sobre números inteiros, ao invés de sobre *strings* de bits. Isto significa que, antes da cifra, uma mensagem precisa ser codificada ou **formatada** para o formato esperado pela primitiva.

Existem múltiplos métodos disponíveis para este processo de formatação. Dois particularmente populares são o PKCS#1 v1.5 e o OAEP (*Optimal Asymmetric Encryption Padding*), ambos padronizados em versões do *standard* PKCS. O PKCS#1 v1.5 é um método mais antigo e, portanto, o OAEP é o método preferível, em geral.

Nesta UC, não nos preocuparemos com detalhes de como cada um destes métodos opera. No entanto, é importante percebermos algumas características gerais desta formatação.

De forma simplificada, esta formatação é basicamente um *padding*. No caso, os bits da mensagem são complementados até a dimensão da representação binária do valor de $N$ utilizado na chave. Esta nova versão da mensagem adicionada do *padding* é, então, interpretada como um número binário para o cálculo da cifra.

Porém, algumas características particulares das cifras assimétricas fazem com que o uso de um esquema de *padding* simples como o PKCS#5 - estudado há algumas aulas no contexto das cifras simétricas de bloco - não seja seguro. Isto se deve à previsibilidade do *padding* adicionado ao PKCS#5, no qual o valor dos bytes adicionados é igual ao número de bytes adicionados. Para evitar esta previsibilidade, os métodos de formatação para cifras assimétricas utilizam soluções mais sofisticadas que introduzem aleatoriedade no *padding* produzido.

De toda maneira, dado um método de formatação, o processo de uma cifra assimétrica é relativamente simples. Do lado da cifra, a mensagem $m$ é passada pelo método de formatação (*e.g.*, o OAEP) que introduz a quantidade necessária de um *padding* pseudo-aleatório e gera uma representação numérica $M$. A seguir, $M$ é cifrada com a primitiva escolhida (*e.g.*, RSA) utilizando a chave pública $k_e$. O resultado é o criptograma $C = E(k_e)(M)$, posteriormente enviado pelo canal inseguro.

Do lado da decifra, faz-se o processo inverso. Inicia-se pela decifra do criptograma recebido $C'$, utilizando-se a primitiva (*e.g.*, RSA) e a chave privada correspondente ($k_d$). A partir disto, obtém-se $M' = E(k_e)(C')$. Note que, neste ponto, $M'$ ainda é a representação numérica da mensagem. Portanto, o passo seguinte é a execução da rotina de **desformatação** (*e.g.*, do OAEP), que transformará o número de volta à representação como uma sequência de bits através (entre outras coisas) da remoção do *padding*. Deste processo, resulta uma mensagem decifrada $m'$. 

Se não houve problemas neste processo (por exemplo, se o criptograma C não foi alterado em trânsito), então $m' = m$, completando a comunicação.

## Cifra Assimétrica: Resumo

Em resumo, uma cifra assimétrica é composta por duas componentes: uma primitiva criptográfica assimétrica (*e.g.*, RSA) e um método auxiliar de formatação (*e.g.*, PKCS#1 v1.5 ou OAEP). Note que é possível utilizar uma mesma primitiva com diferentes métodos de formatação, pelo que a escolha por um método de formatação específico deve levar em conta fatores como as recomendações mais atuais, por exemplo. Isto dá origem a diferentes possíveis esquemas de cifra assimétrica, como o `RSA+PKCS#1 v1.5` ou o `RSA+OAEP`.


O método de formatação têm múltiplas utilidades. Em primeiro lugar, está a codificação da mensagem em texto plano ao domínio específico da função de cifra da primitiva utilizada. Como parte disto, estes métodos acabam por introduzir *padding* aleatório, o que aumenta a resistência da cifra contra determinados tipos de ataque. O *padding* aleatório também acaba por evitar alguns casos especiais. Por exemplo, no RSA, se tentarmos cifrar uma mensagem $M = 0$, obtemos um texto cifrado $C = 0$, independentemente das chaves. Métodos de formatação como o OAEP impedem o mapeamento de mensagens em texto plano $m$ para este valor especial de $M$, independentemente da composição de $m$.

É de se notar, também, que métodos de formatação não utilizam de nenhuma forma as chaves pública ou privada. Ao contrário, estas chaves são utilizadas apenas durante a execução das funções de cifra ou decifra da primitiva.

> [!NOTE]
> Ilustração de uso do RSA (com um método de formatação) para cifrar/decifrar uma mensagem.
>
>    - Cifrar uma mensagem usando a chave pública de Alice:
>
>    ```
>    # openssl pkeyutl -encrypt -inkey alice_public_key.pem -pubin -in mensagem_rsa.txt -out mensagem.cif
>    ```
>
>    - Inspecionar mensagem cifrada e compará-la à mensagem em texto plano. Observar particularmente a diferença em tamanho dos ficheiros.
>    
>    - Decifrar a mensagem com a chave privada de Alice:
>
>    ```
>    # openssl pkeyutl -decrypt -inkey alice_private_key.pem -in mensagem.cif -out mensagem_rsa_decifrada.txt
>    ```
>


