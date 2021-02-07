# Ajuda de configuração

Configurar o FairEmail é bastante simples. Você precisará adicionar pelo menos uma conta para receber e-mail e pelo menos uma identidade se você quiser enviar e-mail. The quick setup will add an account and an identity in one go for most providers.

## Requisitos

É necessária uma conexão com a internet para configurar contas e identidades.

## Configuração rápida

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

This will work for most email providers.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Configurar conta - para receber e-mails

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Selecione um provedor da lista, insira o nome de usuário, que geralmente é seu endereço de e-mail, e insira sua senha. Toque em *Verificar* para permitir que o FairEmail conecte ao servidor de e-mail e possa buscar uma lista de pastas do sistema. Após rever a seleção de pasta do sistema, você pode adicionar a conta tocando em *Salvar*.

If your provider is not in the list of providers (there are thousands of providers), select *Custom*. Digite o nome do domínio, por exemplo *gmail.com* e toque em *Obter configurações*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Para saber mais sobre isso, por favor clique [aqui](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidade - para enviar e-mails

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Digite o nome que você deseja que apareça no remetente dos e-mails que você envia e selecione uma conta vinculada. Toque em *Salvar* para adicionar a identidade.

Se a conta foi configurada manualmente, provavelmente você precisará configurar a identidade manualmente também. Digite o nome do domínio, por exemplo *gmail.com* e toque em *Obter configurações*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Veja [este FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre o uso de apelidos (aliases).

## Conceder permissões - para acessar informações de contato

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Configurar otimizações de bateria - para receber e-mails continuamente

Nas versões recentes do Android, o Android colocará aplicativos para hibernar quando a tela estiver desligada por algum tempo para reduzir o uso de bateria. Se você deseja receber novos e-mails sem atrasos, você deve desativar as otimizações de bateria para o FairEmail. Tap *Manage* and follow the instructions.

## Perguntas ou problemas

Se você tiver dúvidas ou problemas, por favor [clique aqui](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para obter ajuda.