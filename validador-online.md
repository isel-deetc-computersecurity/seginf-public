## Validador JWT/JWE online: https://dinochiesa.github.io/jwt/

Passo a passo para usar o validador com chaves dos certificados fornecidos para o trabalho.

- Mudar modo JWS (Signature) para JWE (Encrypt):
    - Escolher ***Encrypted*** ao invés de *Signed*
- Colocar os algoritmos em:
    - **RSA-OAEP**
    - **A256GCM**
- Campo Decode-Header:
    - mudar valor do campo para: `{ "alg": "RSA-OAEP", "enc": "A256GCM"}`
- Campo Decode payload:
    - valor do campo precisar ser texto, entre aspas.
- Campo Private key: (usar os certificados fornecidos no trabalho)
    - Converter pfx para formato pem sem password com OpenSSL (exemplo com Bob_2):
        - `openssl pkcs12 -in certificates-keys/pfx/Bob_2.pfx -noenc -nocerts -out Bob_2_key.pem`
        - A password é: changeit
    - Copiar desde `-----BEGIN PRIVATE KEY-----` até o fim para o campo "Private key" no validador JWT online.
- Campo Public key: (usar os certificados fornecidos no trabalho)
    - Extrair a chave pública da chave privada no formato pem com OpenSSL:
        - `openssl rsa -in Bob_2_key.pem -out Bob_2_pub.pem -pubout`
    - Copiar desde `-----BEGIN PUBLIC KEY-----` até o fim para o campo "Public key" no validador JWT online.
