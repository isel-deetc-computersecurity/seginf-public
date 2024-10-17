## Certificado de exemplo:
- Do site: sapo.pt
  - Exportar pelo browser: sapo.pt.pem
- Do Trabalho 1, em certificates-keys.zip

## Ver um certificado X.509
```
openssl x509 -inform pem -in sapo.pt.pem -text -noout
```

## Descarregar uma CRL de um certificado
- Por `wget` (comando de linha):
  ```
  wget http://crl3.digicert.com/GeoTrustGlobalTLSRSA4096SHA2562022CA1.crl
  ```
- Fazer o download pelo navegador.

## Ver a CRL
```
openssl crl -in GeoTrustGlobalTLSRSA4096SHA2562022CA1.crl -text -noout
```

## Ver um pfx (password: changeit)
```
openssl pkcs12 -in certificates-keys/pfx/Bob_1.pfx -info -legacy
```

## Verificar caminho de certificação de um certificado
- Para o exemplo sapo.pt.pem
  - Necessário descarregar os certificados intermédio e raiz (que vêm juntos ao certificado sapo.pt.pem).
```
openssl verify -verbose -CApath . -CAfile DigiCert-root.pem -untrusted GeoTrust-CA1.pem -purpose any sapo.pt.pem
```
