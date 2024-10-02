## Considerations
- This examples consider RSA cypher and digital signature with RSA algorithm.
- For the following examples, consider a small text file [message.txt](message.txt).
- `xxd <filename>` is a command-line for MacOS/Linux to show the hexadecimal dump from a file.
  - The equivalent command for Windows Powershell is: `Format-Hex <filename>`

### Generate a key pair (private and public) with 2048 bits:
  ```bash
  openssl genrsa -out alice_private_key.pem 2048
  cat alice_private_key.pem
  ```

### Show the internal parts from key pair (only decode the file .pem from Base64), as modulus, publicExponent and privateExponent:
  ```bash
  openssl rsa -in alice_private_key.pem -text
  ```

### Extract the public key from file .pem:
  ```bash
  openssl rsa -in alice_private_key.pem -out alice_public_key.pem -pubout
  cat alice_public_key.pem
  ```

### Show the internal parts from public key (only decode the file .pem from Base64): modulus and publicExponent:
  ```bash
  openssl rsa -pubin -in alice_public_key.pem -text
  ```

### Encrypt the file message.txt with RSA:
  ```bash
  xxd message.txt
  openssl pkeyutl -encrypt -inkey alice_public_key.pem -pubin -in message.txt -out message_rsa.cif
  xxd message_rsa.cif
  ```

### Decrypt the message_rsa.cif with RSA:
  ```bash
  openssl pkeyutl -decrypt -inkey alice_private_key.pem -in message_rsa.cif -out message_rsa_decrypt.txt
  cat message_rsa_decrypt.txt
  ```

### Generate a signature for the file message.txt:
  ```bash
  openssl dgst -sha256 -sign alice_private_key.pem -keyform PEM -out message.sig message.txt
  ```

### Verify the signature:
  ```bash
  openssl dgst -sha256 -verify alice_public_key.pem -signature message.sig message.txt
  ```
