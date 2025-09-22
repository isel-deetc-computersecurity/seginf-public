[Changelog]: # (v0: versão inicial por Diego Passos)

# Aula 05 - Funções *Hash* Criptográficas

Na aula passada, estudamos os esquemas MAC e vimos como eles resolvem o problema da verificação de integridade / autenticidade de mensagens. Em particular, discutimos como estes esquemas baseiam-se na geração de uma marca (ou *tag*) a partir da mensagem, permitindo a verificação da autenticidade da mesma. Ao fim da aula, discutimos como certos modos de operação combinam cifra e MAC em um único algoritmo (em particular, vimos o modo GCM).

Como parte da discussão sobre esquemas MAC na aula passada, destacou-se que, idealmente, a marca gerada deveria ter um **tamanho fixo**. Isto é importante porque algumas possíveis formas de implementar-se um esquema MAC acabam por gerar marcas de tamanho variável e, algumas vezes, de tamanho comparável ao tamanho da mensagem original. Isto é indesejável, já que dificulta o projeto de protocolos de comunicação segura (em geral, queremos campos de tamanho fixo nos *headers* das mensagens) e eventualmente torna as mensagens desnecessariamente grandes.

Ao discutirmos este problema, citou-se brevemente a possibilidade de utiliza-se um **resumo** ou ***hash*** da mensagem. Lembre-se também que vimos que métodos como o *checksum* e o CRC - que geram resumos de tamanho fixo para o propósito de verificação de integridade em contextos não criptográficos - não são adequados. Isto ocorre porque é computacionalmente simples **causar propositalmente uma colisão** do *checksum* ou do CRC: por exemplo, a partir de uma mensagem $m$, encontrar uma segunda $m'$ que tenha o mesmo resumo. Para os propósitos de um esquema MAC, precisamos que isto seja computacionalmente infazível.

Na aula de hoje, estudaremos mais a fundo as chamadas **funções *hash* criptográficas**. Ou seja, funções utilizadas para a geração de resumos de mensagens de tamanho arbitrário, mas que apresentam alta resistência contra tentativas deliberadas de geração de colisões. Na prática, as funções *hash* criptográficas são amplamente utilizadas como parte dos esquemas MAC (além de terem outros usos em protocolos criptográficos).

Por fim, estudaremos mais concretamente um dos principais esquemas MAC modernos, o HMAC. Particularmente, veremos como este utiliza um *hash* criptográfico para a geração das marcas.

## Funções de *Hash*

O termo *função de hash* é empregado para denotar qualquer função determinística que receba como entrada uma sequência de bits de tamanho arbitrário e gere como saída uma sequência de bits com tamanho fixo. Matematicamente, podemos definir uma função de *hash* $H$ como: $`H: \{0,1\}^* \mapsto \{0,1\}^n`$. Aqui, $n$ denota o comprimento da saída da função - *i.e.*, a **dimensão do *hash***.

Note que é muito fácil definirmos funções com estas características. Por exemplo, podemos definir uma função de *hash* $H_1$ trivial como:

$$
H_1(m) = 0
$$

O que esta função faz é mapear toda e qualquer mensagem $m$ para um único bit, sempre com o valor 0.

Embora esta função atenda às definições de uma função de *hash*, ela não é particularmente boa. Como todas as mensagens são mapeadas para um mesmo valor, o resumo gerado não diz muito sobre a mensagem correspondente. Idealmente, uma **boa função de *hash*** deveria **balancear** as mensagens do domínio entre os possíveis valores de resumo.

Por exemplo, uma função ligeiramente mais complexa, mas que alcança este objetivo é:

$$
H_2(m) = m\ mod\ 2
$$

Aqui, a mensagem $m$ é tratada como um número inteiro e o resumo criptográfico é simplesmente a sua paridade: retorna-se 0 para mensagens que terminam num bit 0 ou 1 para mensagens que terminam num bit 1. Se considerarmos todas as (infinitas) possíveis sequências de bits, metade terá resumo 0, enquanto a outra metade terá resumo 1.

Mesmo a função $H_2(.)$ sendo superior à função $H_1(.)$ para uso geral como *hash*, ela ainda não é uma boa função de *hash* criptográfico. Isso porque uma das propriedades desejáveis de uma boa função de *hash* criptográfico é que seja difícil encontrar duas mensagens diferentes $m$ e $m'$ tal que ambas possuam o mesmo *hash*. No caso da função $H_2(.)$, basta escolher qualquer mensagem $m'$ cujo último bit tenha o mesmo valor do último bit de $m$. Portanto, precisaremos de funções mais sofisticadas para uso criptográfico.

Nota-se pelos exemplos dados até aqui e pela própria definição formal que **qualquer função de *hash*** é, necessariamente, **não-invertível** - em particular, estas funções não são injetivas. Isto é uma consequência direta do facto de que o domínio da função é infinito (*i.e.*, há infinitas sequências possíveis com uma quantidade arbitrária de bits), enquanto seu contra-domínio é finito (*i.e.*, para um dado $n$ fixo, há exatamente $2^n$ possíveis valores de resumo retornáveis pela função). Como o domínio tem cardinalidade superior ao contra-domínio, certamente **haverá mensagens diferentes que terão necessariamente um mesmo resumo**.

Do ponto de vista criptográfico, esta característica é positiva, porque, para as aplicações típicas dos *hashes* criptográficos, não queremos ser capazes de obter $m$ dado o valor de $H(m)$.

> [!NOTE]
> Ilustração de uso de uma função *hash* criptográfica real.
>
>    - Calcular o *hash* SHA-256 de uma *string*:
>
>    ```
>    # echo "Uma mensagem qualquer" | openssl dgst -sha256 
>    SHA2-256(stdin)= fe628ff5be7fdb21c20adb91152d1c669f7fd4e712df4b2130d2bb8baca60cd7
>    ```
>
>    - Observar como pequenas mudanças na mensagem causam grandes efeitos:
>
>    ```
>    echo "uma mensagem qualquer" | openssl dgst -sha256 
>    SHA2-256(stdin)= 9a6b171eb226020725c11ad2e7c697abe223e96d93e9ceb7d8943c0a26586705
>    ```

## Pré-imagem, Segunda Pré-Imagem e Colisões

Uma boa função de *hash* criptográfico $H(.)$ deve apresentar as seguintes propriedades:

- Deve ser computacionalmente fácil calcular $H(x)$ dado $x$. Em outras palavras, gostaríamos de uma função de *hash* com baixa complexidade computacional e que possa ser implementada de maneira eficiente.
- Dado $x$, deve ser computacionalmente **difícil** obter um $x' \not= x$ tal que $H(x') = H(x)$. No jargão criptográfico, dizemos que a função $H(.)$ deve ter resistência a **segunda pré-imagem**.
- Deve, ainda, ser computacionalmente difícil encontrar dois valores quaisquer $x$ e $x'$, com $x' \not= x$, tal que $H(x') = H(x)$. No jargão criptográfico, dizemos que a função $H(.)$ deve ter resistência a **colisão**.

Neste jargão, o termo **imagem** diz respeito aos possíveis valores retornados pela função de *hash*. Desta forma, uma **pré-imagem** associada a uma imagem $y$ é um valor $x$ tal que $H(x) = y$. Como, por natureza das funções de *hash*, $H(.)$ é não-injetiva, conclui-se que pode haver outras pré-imagens de $y$, além do próprio $x$ - ou seja, uma **segunda pré-imagem**, uma **terceira pré-imagem**, ... Na prática, para boas funções de *hash*, é comum haver infinitas pré-imagens para cada imagem $y$. Entretanto, encontrar uma segunda pré-imagem $x'$, dados $x$ e $y$, deve ser computacionalmente infazível.

Por outro lado, na **colisão**, não queremos encontrar um $x'$ associado a uma imagem e uma pré-imagem específicas. Ao contrário, o objetivo é encontrar quaisquer dois valores $x$ e $x'$ que tenham uma certa imagem em comum.

A depender do contexto, tanto colisões quanto segundas pré-imagens podem ser utilizadas por atacantes para circundar soluções de segurança informática. Por isso, a importância de que as funções de *hash* sejam resistentes a ambas. 

Perceba, ainda, que a **resistência a colisão implica resistência a segunda pre-imagem**. Dito de outra forma: se, dado um $x$ é fácil encontrar um $x'$ que partilha a mesma imagem, então, para encontrar um par qualquer $(x, x')$ de pré-imagens que partilhem uma mesma imagem, basta escolhermos um $x$ arbitrário.

Em certos contextos, podemos, ainda, estar interessados na **resistência a pré-imagens** de um *hash*. Enquanto na **resistência a segunda pré-imagem**, assume-se que o atacante conhece $x$ e $y$ tal que $y = H(x)$, na **resistência a pré-imagens**, o atacante possui apenas uma imagem $y$ e deseja encontrar alguma pré-imagem $x$ tal que $y = H(x)$.

Um dos fatores que influenciam a resistência de funções de *hash* a pré-imagens, segundas pré-imagens e colisões é a dimensão do *hash*. Lembre-se que a dimensão do *hash* é o número de bits do resumo gerado como saída da função. Quanto mais bits há no resumo, maior o contra-domínio da função e, em geral, maior o número de imagens distintas que podem ser retornadas. Assumindo-se o uso de uma boa função de *hash* - *i.e.*, que tem um mapeamento equilibrado entre as pré-imagens e as imagens -, quanto mais imagens diferentes podem ser geradas, menor a probabilidade de duas pré-imagens quaisquer partilharem uma mesma imagem. Por exemplo, o `SHA256`, uma função de *hash* criptográfico bastante popular, gera resumos de 256 bits, pelo que há $2^{256}$ possíveis imagens. Assim, a probabilidade de duas pré-imagens distintas terem uma mesma pré-imagem é de $\frac{1}{2^{256}}$.

É importante notar, no entanto, que o número de bits do resumo gerado por uma função de *hash* não é o único fator a determinar sua resistência a estes ataques. Por exemplo, considere a seguinte hipotética função de *hash* $H_3(.)$:

$$
H_3(m) = \underbrace{000\dots 0}_{256\ bits}
$$

Ou seja, a função sempre retorna uma sequência de 256 bits zero, independentemente da mensagem $m$. Embora $H_3(.)$ tenha a mesma dimensão da `SHA256`, claramente ela não é resistente a segunda pré-imagem. Por exemplo, dado $x = 1$, que resulta em $y = 000\dots 0$, trivialmente podemos escolher $x' = 2$, que também resulta na mesma imagem. Similarmente, $H_3(.)$ também não é resistente a colisões e nem a pré-imagem.

## Outras Propriedades e Características de *Hashes* Criptográficos

Boas funções de *hash* criptográfico servem como uma impressão digital, ou *fingerprint*. Ou seja, elas podem ser utilizadas como **representante** de uma determinada mensagem $m$, capturando, de alguma forma, características de $m$. 

Funções de *hash* criptográfico geralmente apresentam grande dimensão pelos motivos explicados na seção anterior. Por exemplo, já citamos o SHA-256 que gera resumos de 256 bits, mas mesmo outras funções, hoje consideradas obsoletas, têm dimensão elevada, como o SHA-1 (160 bits) e o MD5 (128 bits).

Embora o resumo criptográfico gerado por estas funções seja grande, em geral, elas quebram a mensagem original em palavras de pequena dimensão (*e.g.*, 16, 32 ou 64 bits). Estas palavras são, então, combinadas através de operações booleanas (bit a bit), aritméticas, além de *shifts* e rotações.

O resultado da complexidade das operações utilizadas em funções de *hash* modernas é que pequenas alterações em uma mensagem tendem a gerar grandes alterações nos respetivos resumos. Por exemplo, suponha duas mensagens $m$ e $m'$ quase idênticas, exceto pelo valor de um único bit. Uma boa função de *hash* criptográfico tende a gerar resumos bastante diferentes para as duas.

## Aplicações de Funções de *Hash* Criptográficas

Funções de *hash* criptográficas têm diversas aplicações em segurança da informação.

Uma delas é para a verificação de integridade de dados. Por exemplo, suponha que queiramos fazer *download* a uma aplicação a partir de um servidor da Internet. Um atacante poderia aproveitar-se desta situação para tentar substituir a versão legítima da aplicação por outra modificada para realizar alguma ação maliciosa. Ao recebermos o *download*, o ideal seria, portanto, termos alguma capacidade de verificar se o o ficheiro recebido é, realmente, o original. Se o desenvolvedor da aplicação nos fornece de alguma forma segura o *hash* do ficheiro legítimo, podemos calcular o *hash* da versão recebida e compará-las: se os *hashes* são diferentes, temos certeza de que trata-se de um ficheiro não-íntegro.

O transmissão segura do *hash* da versão íntegra do ficheiro, no entanto, é uma tarefa difícil em um canal de comunicação inseguro. Assim, o ideal é a utilização de um esquema MAC que usa uma chave partilhada para a verificação da autenticidade. No entanto, como veremos a seguir mais concretamente, mesmo esquemas MAC também utilizam frequentemente *hashes* criptográficos como parte da sua operação. Neste caso, é comum vermos tais esquemas MAC referidos como **função de *hash* com chave** (ou, em Inglês, *Keyed Hash Function*).

Outro mecanismo similar ao MAC (porém, baseado em criptografia assimétrica), a assinatura digital também utiliza funções de *hash* criptográficas para propósitos similares.

Outro uso comum das funções de *hash* criptográficas é para a **geração de chaves** a partir de palavras-passe. Considere, por exemplo, o uso de uma primitiva criptográfica AES com chaves de 128 bits. Se gerarmos uma chave de 128 bits aleatoriamente, um utilizador dificilmente conseguirá decorá-la. Por outro lado, palavras-passe tendem a ser mais amenas ao utilizador, mas não terão o formato esperado pelo AES. Uma saída para este problema é permitir que o utilizador especifique a chave na forma de uma palavra-passe (*i.e.*, uma *string* de caracteres), mas aplicar uma função de *hash* criptográfico com dimensão adequada para transformá-la nos 128 bits necessários ao AES. Como as funções de *hash* são determinísticas, a mesma palavra-passe sempre é mapeada para a mesma chave.

Embora estes sejam os usos clássicos das funções de *hash* criptográficas, protocolos criptográficos práticos frequentemente as utilizam para vários outros propósitos. Por exemplo, tecnologias de cadeias de bloco (ou, no Inglês, *blockchains*), utilizam extensivamente funções de *hash*. Em parte, este uso tem por objetivo garantir a integridade dos dados armazenados. Porém, funções de *hash* também têm sido usadas para o propósito de **prova de trabalho**. Neste caso, o protocolo especifica um desafio computacional que geralmente envolve encontrar uma pré-imagem que associada a uma imagem com determinadas características. O objetivo e ter certeza que determinada entidade investiu esforço computacional suficiente para que seja considerada confiável para determinado propósito. Mais especificamente, confia-se que a entidade é legítima, porque o resultado de um determinado ataque não valeria o esforço computacional realizado.

## O Esquema HMAC

O HMAC é um exemplo de função *hash* com chave. Mais especificamente, trata-se de um conjunto de algoritmos MAC que podem ser utilizados com diferentes funções de *hash* $H(.)$.

Abstratamente, o conceito básico do HMAC é simples: a marca é resultado da aplicação do *hash* $H(.)$ a uma combinação da chave simétrica $k$ com a mensagem $m$ que se deseja proteger. Assumindo-se que $H(.)$ seja uma boa função *hash* criptográfica, mesmo pequenas alterações feitas à mensagem $m$ devem ter impacto significativo no resultado do *hash* e, portanto, na marca correspondente à mensagem. Ou seja, dado que o *hash* é resistente a segunda pré-imagem, é altamente improvável que o atacante consiga alterar $m$ para uma outra mensagem $m'$ que, por acaso, tem a mesma marca. Além disso, se o atacante não conhece a chave $k$, ele não deve ser capaz de gerar uma nova marca que seja consistente com a mensagem alterada $m'$.

Mais concretamente, o HMAC funciona da seguinte forma. A partir da chave $k$, calculam-se dois valores: $k \oplus opad$ e $k \oplus ipad$. Aqui, $ipad$ e $opad$ denotam constantes definidas pelo método: $opad = 0x5c5c5c...5c$ e $ipad = 0x363636...36$. A seguir, calcula-se $H((k \oplus ipad) || m)$. O resultado desta aplicação da função $H(.)$ é, então, concatenado com $k \oplus opad$, resultando em $(k \oplus opad) || H((k \oplus ipad) || m)$. Finalmente, calcula-se novamente a função $H(.)$, agora sobre este último valor. Portanto:

$$
HMAC(k, m) = H((k \oplus opad) || H((k \oplus ipad) || m))
$$

Deve-se notar que esta estrutura geral é aplicável a qualquer função de *hash*. Assim, ao utilizarmos o HMAC, devemos especificar qual função de *hash* utilizar. Por exemplo, podemos utilizar o HMAC em conjunto com a SHA-256, dando origem ao esquema MAC `HMAC-SHA256`.

O HMAC também é aplicável independentemente do tamanho da chave. Logo, pode-se optar por chaves mais curtas ou mais longas a depender das necessidades específicas de cada caso. Como habitual, chaves mais longas são inerentemente mais seguras por dificultarem ataques por força bruta.

Destaque-se, ainda, que o tamanho do resumo gerado pelo HMAC é fixo - **para uma dada função de *hash*** - independentemente do tamanho da chave ou do tamanho da mensagem. Isto é uma consequência direta do uso de funções de *hash* e uma vantagem significativa para a maior parte das aplicações.


> [!NOTE]
> Ilustração da geração de um MAC através do `OpenSSL`.
>    - Exemplificar comando para geração de um MAC usando HMAC:
>
>    ```
>    # openssl dgst -hmac mysecretkey mensagem.txt
>    HMAC-SHA256(mensagem.txt)= 078649e2431fd24a882b81cbf9879fe3cdd48400affb267b54ec12d62e693aec
>    ```
>
>    - Observar como mesmo alterações muito pequenas na mensagem original resultam em marcas potencialmente muito diferentes (e.g., para um ficheiro `mensagem_alterada.txt` com apenas uma letra diferente):
>
>    ```
>    # openssl dgst -hmac mysecretkey mensagem_alterada.txt
>    HMAC-SHA256(mensagem_alterada.txt)= 19e0e5120fecf255c955df144724877acbcd5574b6e96761a9d929d9cce1d57e
>    ```
