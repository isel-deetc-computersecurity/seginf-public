## Validador JWT/JWE online: https://dinochiesa.github.io/jwt/

Passo a passo para usar o validador com chaves dos certificados fornecidos para o trabalho.

- Mudar modo JWS (*Json Web Signature*) para JWE (*Json Web Encrypt*) escolhendo ***Encrypted*** ao invés de *Signed*
- Colocar os algoritmos em:
    - **RSA-OAEP**
    - **A256GCM**
- Campo Decode-Header:
    - mudar valor do campo para: `{ "alg": "RSA-OAEP", "enc": "A256GCM"}`
- Campo Decode payload:
    - Em caso de processo de cifrar, o valor deste campo precisa ser texto, entre aspas.
    - Em caso de processo de decifrar, o valor deste campo será alterado quando o botão `✔` é carregado.
- Campo Private key: (usar os certificados fornecidos no trabalho)
    - Converter pfx para formato pem sem password com OpenSSL (exemplo com Bob_2):
        - `openssl pkcs12 -in certificates-keys/pfx/Bob_2.pfx -noenc -nocerts -out Bob_2_key.pem`
        - A *password* é: `changeit`
    - Copiar desde `-----BEGIN PRIVATE KEY-----` até o fim para o campo "Private key" no validador JWT online.
- Campo Public key: (usar os certificados fornecidos no trabalho)
    - Extrair a chave pública da chave privada no formato pem com OpenSSL:
        - `openssl rsa -in Bob_2_key.pem -out Bob_2_pub.pem -pubout`
    - Copiar desde `-----BEGIN PUBLIC KEY-----` até o fim para o campo "Public key" no validador JWT online.
