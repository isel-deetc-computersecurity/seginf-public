# Aula 04 - Esquemas MAC

Na aula passada, vimos como primitivas simétricas podem ser utilizadas para o propósito de cifra: ou seja, em esquemas criptográficos que objetivam garantir a confidencialidade.

Desde o início do curso, no entanto, citamos algumas vezes que criptografia também pode ser utilizada para garantir duas outras propriedades da segurança da informação: a integridade e a autenticidade. Nesta aula, estudaremos o tipo de esquema criptográfico simétrico utilizado para isso: o chamado *Message Authentication Code*, ou MAC. Também falaremos brevemente do GCM (do Inglês, *Galois Counter Mode*), um modo de operação que combina cifra simétrica e MAC em um único algoritmo.

## Autenticação de Mensagens

A autenticação de mensagens é o processo através do qual o leitor/receptor de uma determinada informação é capaz de validar que uma mensagem lida/recebida é autentica. Uma mensagem é autentica quando encontra-se íntegra (*i.e.*, não sofreu nenhuma alteração não autorizada durante a transmissão/armazenamento) e foi gerada garantidamente pela entidade que se acredita ser a autora.

De um ponto de vista abstrato, gostaríamos que a entidade que origina uma mensagem fosse capaz de anexar algum tipo de marca verificável de autenticidade antes de enviá-la pelo canal inseguro - processo chamado de **autenticação**. Do lado leitor/receptor, gostaríamos que houvesse um algoritmo que fizesse a **verificação** da mensagem e da marca em conjunto e tivesse uma saída binária: a mensagem é autêntica ou não.

Como estamos ainda a discutir as tarefas criptográficas no âmbito da criptografia simétrica, vamos novamente assumir que as partes legítimas da comunicação - digamos, Alice e Bob - estabeleceram alguma chave simétrica partilhada de forma segura. Em outras palavras, Alice e Bob conhecem a chave, mas nenhuma terceira parte não autorizada tem acesso à chave. Esta única chave partilhada é usada tanto no processo de autenticação - feito por Bob - quanto no processo de verificação - feito por Alice.

## Esquema MAC

O problema descrito na seção anterior pode ser resolvido com base em primitivas de criptografia simétrica. A solução neste contexto recebe o nome de MAC (do Inglês, *Message Authentication Code*). Como o nome sugere, o MAC consiste num código de autenticação (muitas vezes denominado *marca* ou *tag*) gerado a partir da mensagem e de uma chave partilhada que, em conjunto com a chave, pode ser usado no receptor para verificação.

Abstratamente, um esquema MAC é constituído por 3 funções básicas:

- Uma função $G(.)$ de **geração de chaves**, utilizada para gerar a chave $k$ partilhada usada no restante do esquema. Assim como nas cifras simétricas, em geral, $k$ é gerada aleatoriamente para que não possa ser previsível para um atacante.
- Uma função $T(.)$ de **geração de marcas**. Esta função tem como argumentos a mensagem $m$ que se deseja autenticar e a chave partilhada $k$. A saída é uma marca $t$ a ser enviada juntamente com a mensagem $m$. Matematicamente, $t = T(k)(m)$.
- Uma função $V(.)$ de **verificação de marcas**. Esta função recebe como parâmetros a chave partilhada $k$, a mensagem $m$ que se deseja verificar e a marca $t$ gerada anteriormente. Como saída, a função retorna um valor binário `true`, se $m$ é íntegro e autêntico, ou `false`, caso contrário.

É importante observar que um esquema MAC deve suportar mensagens $m$ de tamanho arbitrário. Matematicamente, $m \in \{0, 1\}^*$. Além disto, note que, **se confidencialidade não é um requisito**, tanto a mensagem $m$ quanto a marca $t$ podem ser **transmitidas em texto plano** pelo canal de comunicação inseguro: a capacidade de um atacante ver o conteúdo de $m$ e $t$ não deve afetar a efetividade do esquema MAC. 

Mas como podemos realizar um esquema MAC concretamente? O que exatamente é a marca gerada pela função $T(.)$ e como podemos garantir que um atacante não consegue forjá-la para uma mensagem não autentica? Além disto, como exatamente funcionaria a função de verificação $V(.)$? Há muitas possíveis variações factíveis de esquemas MAC, então analisaremos algumas abordagens hipotéticas.

Como uma primeira tentativa, considere a ideia de que a marca $t$ é um pequeno documento digital que contém informações sobre a informação a ser autenticada: *e.g.*, o nome da entidade autora da informação, o tamanho da mensagem $m$ em bits, entre outras. Ao receber a mensagem e a marca pelo canal inseguro, Alice verificaria a identidade da entidade autora para garantir que trata-se de Bob e faria verificações das demais características da mensagem (por exemplo, se o tamanho da mensagem recebida corresponde ao que é informado na marca).

Não é difícil ver que esta abordagem não é eficaz. Isso porque um atacante que modifique uma mensagem legítima ou forge uma mensagem própria pode perfeitamente forjar também a marca, preenchendo-a com informações consistentes com a sua mensagem falsa, mas com a identificação de Bob. Note, ainda, que em momento algum esta abordagem utilizou a chave partilhada entre Bob e Alice.

O problema desta abordagem encontra-se justamente no facto de qualquer um poder facilmente gerar versões da marca consistentes com mensagens falsas. Logo, precisamos de uma alternativa em que apenas uma das partes legítimas consiga gerar uma marca com informações consistentes com a mensagem que a acompanha. No cenário proposto, uma das coisas que caracteriza as partes legítimas é precisamente o conhecimento da chave partilhada.

Consideremos, então, uma segunda abordagem: a função $T(.)$ gera uma marca que contém exatamente as mesmas informações utilizadas na abordagem anterior, mas agora **cifrada com a chave k**. Em outras palavras, $t = E(k)(M)$, onde $M$ denota *metadados* da mensagem original $m$. Por outro lado, a função de verificação $V(.)$ executada por Alice decifra os metadados calculando $M' = D(k)(t)$ e compara propriedades lá listadas com a mensagem $m'$ recebida. Se a verificação tiver sucesso, assume-se que $m' = m$ é autentica.

Este segundo método é mais bem-sucedido que o primeiro? Ele não está susceptível aos mesmos problemas do anterior? 

Vamos considerar como um atacante poderia tentar realizar um ataque a este esquema MAC. Digamos que o atacante tente gerar uma mensagem forjada do zero. Como no caso anterior, ele precisa gerar também a marca correspondente. Para isto, ele pode facilmente construir os metadados com as informações que quiser: a identificação de Bob e as características consistentes com a mensagem forjada. Entretanto, o atacante esbarra em um obstáculo na última fase da geração da marca: cifrar os metadados com a chave $k$. Isso porque ele não conhece a chave partilhada por Bob e Alice. Logo, o melhor que o atacante pode fazer é cifrar os metadados com uma chave aleatória qualquer. Entretanto, como a chave utilizada dificilmente será a mesma partilhada por Bob e Alice, quando Alice realizar a decifra, obterá um texto plano diferente dos metadados criados pelo atacante e, portanto, os metadados decifrados não corresponderão às características da mensagem forjada, permitindo à Alice detetar a falta de autenticidade.

De forma resumida, esta segunda abordagem é mais bem-sucedida porque utiliza a capacidade de cifrar informações - neste caso, os metadados - com a chave partilhada - que apenas Bob e Alice conhecem - como uma prova de que a mensagem é autêntica. 

No entanto, para afirmarmos definitivamente que este esquema MAC funciona, é necessário pensarmos mais cuidadosamente em quais informações estão incluídas na marca. Por exemplo, digamos que os metadados da marca possuam apenas a identificação da origem e o tamanho da mensagem $m$. Agora suponha que o atacante interceta uma mensagem $m$ legítima de Bob para Alice contendo uma marca válida $t$. O atacante pode livremente alterar quaisquer bits de $m$ **sem mudar o tamanho da mensagem original**, obtendo uma mensagem corrompida de mesmo tamanho $m'$, e depois enviar $m'$ e $t$ para Alice. Todas as verificações de Alice serão bem sucedidas, já que $t$ foi cifrada com a chave partilhada correta e, quando decodificada, informará a identidade de Bob e um tamanho consistente com a mensagem $m'$.

Portanto, identificamos assim duas características-chave de uma marca para um esquema MAC:

1. A marca deve depender, de alguma forma verificável, da chave partilhada $k$.
2. A informação transportada pela marca deve permitir uma identificação (quase) inequívoca da mensagem original.

Dados estes dois requisitos, considere agora uma terceira proposta de um esquema MAC hipotético. Nesta terceira proposta, a função $T(.)$ consiste simplesmente na cifra da mensagem $m$ que se deseja autenticar: $t = T(k)(m) = E(k)(M)$. Analogamente, a função de verificação $V(.)$ consiste em cifrar a mensagem recebida e comparar o criptograma resultante com a marca recebida. Mais formalmente, o receptor:

1. Computa $m' = E(k)(m)$.
2. Compara $m'$ e $m$.
    - Se foram iguais, declara-se que a mensagem é íntegra e autêntica.
    - Caso contrário, a mensagem não é integra ou não é autêntica.

O quão resistente este esquema é relativamente aos ataques anteriores? Se um atacante tenta construir uma mensagem $m''$ forjada do zero, por não conhecer a chave correta $k$, ele não conseguirá computar um criptograma que decodifique para o conteúdo exato de $m''$. Logo, a verificação de Alice deverá detectar que se trata de uma mensagem falsa. Por outro lado, se o atacante interceta uma mensagem legítima de Bob e altera bits aleatoriamente, ele precisaria, sem conhecer $k$, conseguir fazer alterações pontuais no criptograma de forma que, ao ser decifrado, este resultasse na mesma mensagem alterada. Assumindo que isso seja infazível computacionalmente na cifra simétrica utilizada (note que alguns esquemas permitem este tipo de modificação), o atacante não seria bem sucedido.

## Propriedades do MAC

Assim como fizemos para as cifras simétricas, é possível listar uma série de propriedades que bons esquemas MAC devem apresentar.

Destas, a propriedade mais básica é a da correção. Um esquema MAC correto garante que a verificação nunca irá falhar caso as três condições abaixo ocorram simultaneamente:

1. Os processos de autenticação e verificação sejam feitos com a mesma chave $k$.
2. A mensagem original $m$ não sofra alterações.
3. A marca $t$ não sofra alterações.

Matematicamente, isso equivale a:

$$\forall m \in \{0, 1\}^*, \forall k \in Keys: V(k)(T(k)(m), m) = true$$

Note que a propriedade acima não impõe, adicionalmente, que a função $V(.)$ retorne um valor falso sempre que aplicada a uma mensagem diferente daquela a partir da qual a marca foi gerada. Em outras palavras, **não exige-se** de um esquema MAC correto que:

$$\forall m, m' \in \{0, 1\}^*, m\not= m',  \forall k \in Keys: V(k)(T(k)(m), m') = false$$

De facto, como veremos na próxima aula mesmo bons esquemas MAC são susceptíveis a **colisões**: duas mensagens diferentes *podem* ter um mesmo valor de marca, embora, idealmente, a probabilidade de ocorrência disto deve ser muito baixa.

Do ponto de vista da segurança, considera-se que um esquema MAC é seguro se duas propriedades são **simultaneamente** atendidas:

1. Resistência a **falsificação seletiva**: dada uma mensagem **específica** $m$ que o atacante deseja enviar ao seu alvo, deve ser computacionalmente infazível encontrar uma marca $t$ tal que $V(k)(t, m) = true$ sem conhecimento da chave $k$.
2. Resistência a **falsificação existencial**: deve ser computacionalmente infazível encontrar qualquer par $(t, m)$ tal que $V(k)(t, m) = true$ sem conhecimento da chave $k$.

Do ponto de vista do atacante, a falsificação existencial é mais "fácil", dado que o objetivo é chegar a uma mensagem forjada qualquer. Já a falsificação seletiva é um objetivo mais ambicioso e, se alcançado, tem potencial de gerar problemas mais graves às entidades legítimas.

Assim como no caso dos esquemas de cifra simétrica, esquemas MAC devem suportar mensagens de tamanho arbitrário. Entretanto, **não é desejável** que a marca tenha tamanho variável. Considere, por exemplo, a última proposta de um esquema MAC hipotético apresentado na seção anterior: naquele caso, a marca $t$ era simplesmente uma versão cifrada da mensagem original. Isso significa que $t$ teria comprimento similar (possivelmente maior) que o comprimento de $m$. Para mensagens pequenas, isso não é um problema, mas se a mensagem é, por exemplo, um ficheiro com vários gigabytes, a transmissão ou armazenamento de uma marca de tamanho similar necessitaria de recursos muito significativos.

Ao contrário, o ideal é que a marca tivesse tamanho fixo e (relativamente) pequeno. Na prática, isso é obtido com o uso de mecanismos de **resumo criptográfico** (também chamados de ***hashes* criptográficos**). Similarmente a mecanismos utilizados para a verificação de integridade de dados em outros domínios (e.g., *checksum*, CRC), estes resumos ou *hashes* são funções que processam uma mensagem e geram como saída um valor numérico de comprimento fixo, sendo que alterações na mensagem *normalmente* alteram o valor de saída da função. No âmbito dos esquemas MAC, estes *hashes* são aplicados sobre alguma combinação da chave partilhada com o a mensagem, gerando uma representação de comprimento fixo que pode ser usada como uma marca. 

Mesmo que o formato da saída de métodos como o *checksum* e o CRC sejam similares ao que se obtém com os *hashes* criptográficos, estes métodos não foram criados com segurança em mente, e podem ser facilmente manipulados para obter-se uma falsificação existencial. Assim, é fundamental que estes resumos sejam gerados por métodos de *hash* criptográfico para manter as propriedades de segurança do esquema. Estudaremos mais sobre *hashes* criptográficos em aulas futuras.

Por fim, deve-se notar que um esquema MAC **não é capaz de garantir não-repúdio**. Isto porque esquemas MAC baseiam sua autenticação no conhecimento de uma **chave partilhada**. Logo, existem ao menos duas partes legítimas que conhecem a chave e, portanto, dada uma mensagem e sua marca de autenticação, não é possível afirmar com certeza qual das entidades gerou o par `(mensagem, marca)`.


> [!NOTE]
>
>    Ilustração da geração de um MAC através do `OpenSSL`.
>    
>    - Exemplificar comando para geração de um MAC usando HMAC:
>
>    ```
>    # openssl dgst -hmac mysecretkey mensagem.txt
>    HMAC-SHA256(mensagem.txt)= 078649e2431fd24a882b81cbf9879fe3cdd48400affb267b54ec12d62e693aec
>    ```
>
>    - Destacar na saída do comando a marca gerada.
>    - Ilustrar como o receptor de uma mensagem utilizaria a marca (e a mensagem) para realizar a verificação.
>    - Ilustrar como mesmo alterações muito pequenas na mensagem original resultam em marcas potencialmente muito diferentes (e.g., para um ficheiro `mensagem_alterada.txt` com apenas uma letra diferente):
>
>    ```
>    # openssl dgst -hmac mysecretkey mensagem_alterada.txt
>    HMAC-SHA256(mensagem_alterada.txt)= 19e0e5120fecf255c955df144724877acbcd5574b6e96761a9d929d9cce1d57e
>    ```

## Cifra Autenticada

Confidencialidade e integridade / autenticidade são objetivos criptográficos diferentes. Enquanto cifras simétricas garantem confidencialidade e esquemas MAC garantem integridade / autenticidade, a princípio nem um nem outro garante ambas as propriedades simultaneamente. Se determinada aplicação necessita, simultaneamente, de confidencialidade e integridade / autenticidade, é necessária a utilização de uma combinação dos dois esquemas. 

Uma possibilidade é primeiro cifrar a mensagem e depois gerar a marca sobre o criptograma. Ou seja, transmitir $E(k_1)(m) || T(k_2)(E(k_1)(m))$. Neste caso, o receptor primeiro faz a verificação da **autenticidade do criptograma** através da marca e, em caso de sucesso, procede à decifra da mensagem. Esta abordagem é conhecida como *encrypt-then-MAC*.

Note na expressão que são utilizadas duas chaves diferentes: uma $k_1$, para o esquema de cifra simétrica, e outra $k_2$, para o esquema MAC. Além de haver a possibilidade de os esquemas MAC e de cifra utilizarem chaves com formatos diferentes (digamos, com tamanhos diferentes), é uma boa prática de segurança **não usar** a mesma chave para dois propósitos diferentes dentro de um mesmo protocolo criptográfico. Isto impede que um atacante tente correlacionar as saídas do MAC e da cifra de modo a ganhar algum conhecimento sobre o texto plano ou sobre a chave.

Alternativamente, pode-se realizar o *MAC-then-encrypt*: primeiro computa-se o MAC sobre o texto plano e depois todo o conjunto é cifrado. Matematicamente: $E(k_1)(m || T(k_2)(m))$. Neste caso, o receptor é obrigado a primeiro realizar a decifra, obtendo a mensagem em texto plano e sua marca, seguida da verificação da marca. Observe, novamente, o uso de chaves distintas para tarefas criptográficas distintas.

> [!NOTE]
>
>    Ilustração do *MAC-then-encrypt*.
>
>    **Execução:**
>    
>    - Calcular o MAC sobre a mensagem em texto plano:
>
>    ```
>    # openssl dgst -hmac mysecretkey mensagem.txt
>    HMAC-SHA256(mensagem.txt)= 078649e2431fd24a882b81cbf9879fe3cdd48400affb267b54ec12d62e693aec
>    ```
>
>    - Destacar que o MAC é diferente daquele gerado para o criptograma no *encrypt-then-MAC*.
>    - Anexar o MAC ao ficheiro:
>
>    ```
>    # cp mensagem.txt mensagem_mais_mac.txt
>    # openssl dgst -hmac mysecretkey -binary mensagem.txt >> mensagem_mais_mac.txt
>    ```
>
>    - Mostrar a mensagem aumentada com o MAC:
>
>    ```
>    hexdump -C mensagem_mais_mac.txt 
>    ```
>
>    - Mostrar que os últimos bytes do ficheiro são idênticos ao MAC do texto plano.
>    - Cifrar a mensagem concatenada com o MAC:
>
>    ```
>    # openssl enc -des-cbc -e -in mensagem_mais_mac.txt -out mensagem.cif -iv 7766554433221100 -K 0011223344556677 -provider legacy
>    ```
>
>    - Ilustrar o processo de verificação:
>
>    ```
>    # openssl enc -des-cbc -d -in mensagem.cif -out mensagem_mais_mac.dec -iv 7766554433221100 -K 0011223344556677 -provider legacy
>    # tail -c 32 mensagem_mais_mac.dec > mac.dec
>    # head -c -32 mensagem_mais_mac.dec > mensagem.dec
>    # openssl dgst -hmac mysecretkey mensagem.dec
>    HMAC-SHA256(mensagem.dec)= 078649e2431fd24a882b81cbf9879fe3cdd48400affb267b54ec12d62e693aec
>    # hexdump -C mac.dec
>    00000000  07 86 49 e2 43 1f d2 4a  88 2b 81 cb f9 87 9f e3  |..I.C..J.+......|
>    00000010  cd d4 84 00 af fb 26 7b  54 ec 12 d6 2e 69 3a ec  |......&{T....i:.|
>    00000020
>
>    ```

Em teoria, o *encrypt-then-MAC* apresenta algumas ligeiras vantagens. Em primeiro lugar, ele permite a verificação da autenticidade antes da decifra, o que economiza tempo caso a mensagem recebida seja forjada. Isso também dificulta alguns ataques específicos de texto cifrado escolhido, como o *padding oracle attack*, brevemente discutido na última aula. Por outro lado, Ferguson and Schneier afirmam que o *encrypt-then-MAC* é mais susceptível a erros por parte do projetista da solução, porque nem sempre é suficiente que o MAC seja aplicado sobre o texto cifrado. Por exemplo, para cifras que utilizam IV, este muitas vezes é transmitido em texto plano junto ao criptograma. Neste caso, é importante que o MAC seja calculado sobre todo o conjunto, incluindo o IV.

Na prática, ambas as abordagens são adotadas, havendo exemplos de protocolos criptográficos considerados seguros que adotam uma ou outra.

### Galois Counter Mode

Além das abordagens *encrypt-then-MAC* e *MAC-then-encrypt* que combinam esquemas de cifra e esquemas MAC separados, existem modos de operação projetados para fornecer confidencialidade e integridade / autenticidade em uma única operação. Exemplos incluem o *Offset Codebook Mode* (OCB), o *Counter with CBC-MAC* (CCM) e o ***Galois Counter Mode* (GCM)**. O GCM, em particular, é amplamente adotado atualmente - por exemplo, é utilizado em certos modos de operação do WPA3, a versão atual da solução de segurança adotada no WiFi.

Em termos de funcionamento, o GCM combina o modo CTR com o cálculo de uma função *hash* chamada GHASH em um único fluxo de operação. Assim como no CTR, gera-se um fluxo de chave utilizando um contador cujo valor inicial é derivado a partir do IV e a chave $k$. Este fluxo de chave é combinado com o texto plano através da operação XOR bit-a-bit, resultando no texto cifrado. 

No entanto, ao invés de limitar-se a geração deste texto cifrado, o modo GCM o utiliza como entrada da função GHASH - portanto, uma abordagem do tipo *encrypt-then-MAC*. Além do texto cifrado, também passa-se pela GHASH um AAD (*Additional Authentication Data*), que é qualquer dado adicional que não deva ser cifrado, mas que necessite autenticação. De forma simplificada, a GHASH corresponde ao cálculo do valor de um polinômio em um ponto específico $H$. Tanto o AAD quanto os blocos de texto cifrado são tratados como coeficientes no cálculo do polinômio. Esta avaliação do polinômio é realizada com operações em no grupo finito GF(128) - de onde vem a referência ao matemático Évariste Galois no nome do modo. O resultado final do GHASH é, então, combinado com o primeiro bloco do fluxo de chave através de um XOR. 

Note, no entanto, que este nível de detalhe sobre a matemática que embasa o método não é relevante para esta UC. Para os nossos propósitos, basta sabermos que o GCM combina cifra e MAC em um único modo, permitindo, adicionalmente, a autenticação de dados não cifrados (o AAD).

