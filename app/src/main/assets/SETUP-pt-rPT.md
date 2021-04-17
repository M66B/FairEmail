# Ajuda de configuração

Configurar o FairMail é bastante simples. Irá precisar de pelo menos uma conta para receber email e pelo menos uma identidade se quiser enviar email. The quick setup will add an account and an identity in one go for most major providers.

## Requisitos

Uma ligação à internet é obrigatória para configurar contas e identidades.

## Configurador rápido

Just select the appropriate provider or *Other provider* and enter your name, email address and password and tap *Check*.

Isto funcionará para a maioria dos provedores de e-mail.

If the quick setup doesn't work, you'll need to set up an account and an identity manually, see below for instructions.

## Configurar conta - para receber email

To add an account, tap *Manual setup and more options*, tap *Accounts* and tap the 'plus' button at the bottom and select IMAP (or POP3). Selecione um provedor da lista, escreva o seu nome de utilizador, que é praticamente o seu endereço de email e escreva a sua palavra-passe. Pressione *Check* para o FairMail o ligar ao servidor de email para ir buscar a lista de pastas do sistema. Depois de rever a seleção da pasta de sistema pode adicionar a conta e pressionar *Save*.

If your provider is not in the list of providers, there are thousands of providers, select *Custom*. Escreva o nome do domínio, por exemple *gmail.com* e pressione *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right IMAP host name, port number and encryption protocol (SSL/TLS or STARTTLS). Para mais informações sobre isto, por favor [here](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidade - para receber email

Similarly, to add an identity, tap *Manual setup and more options*, tap *Identities* and tap the 'plus' button at the bottom. Escreva o nome que quiser que apareça no *from address* dos emails que enviar e selecione uma conta *linked*. Pressione *Save* para adicionar a identidade

Se a conta foi configurada manualmente, provavelmente precisará de configurar a identidade manualmente também. Escreva o nome do domínio, por exemplo *gmail.com* e pressione *Get settings*. If your provider supports [auto-discovery](https://tools.ietf.org/html/rfc6186), FairEmail will fill in the host name and port number, else check the setup instructions of your provider for the right SMTP host name, port number and encryption protocol (SSL/TLS or STARTTLS).

Veja [este FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre usar aliases.

## Conceder permissões - para aceder a informação de contacto

If you want to lookup email addresses, have contact photos shown, etc, you'll need to grant permission to read contact information to FairEmail. Just tap *Grant* and select *Allow*.

## Configurar otimizações de bateria - para continuamente receber emails

Nas versões mais recentes do Android, o Android põe as aplicações em modo de suspensão quando a tela estiver desligada durante algum tempo para reduzir uso da bateria. Se quiser receber novos emails sem atraso, terá que desabilitar as otimizações de bateria para o FairMail. Toque em *Gerenciar* e siga as instruções.

## Perguntas ou problemas

Se tiver uma pergunta ou um problema, por favor [see here](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para ajuda.