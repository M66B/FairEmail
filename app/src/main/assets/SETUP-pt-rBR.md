# Ajuda de configuração

Configurar o FairEmail é bastante simples. Você precisará adicionar pelo menos uma conta para receber e-mail e pelo menos uma identidade se você quiser enviar e-mail. A configuração rápida irá adicionar uma conta e uma identidade de uma só vez para a maior parte dos principais provedores.

## Requisitos

É necessária uma conexão com a internet para configurar contas e identidades.

## Configuração rápida

Basta selecionar o provedor apropriado ou *Outro provedor* e insira seu nome, endereço de e-mail e senha e clique em *Verifique*.

Isto funcionará para a maioria dos provedores de e-mail.

Se a configuração rápida não funcionar, você precisará configurar uma conta e uma identidade manualmente, veja abaixo para obter instruções.

## Configurar conta - para receber e-mails

Para adicionar uma conta, toque em *Configuração manual e mais opções*, toque em *contas* e toque no botão 'mais' na parte inferior e selecione IMAP (ou POP3). Selecione um provedor da lista, insira o nome de usuário, que geralmente é seu endereço de e-mail, e insira sua senha. Toque em *Verificar* para permitir que o FairEmail conecte ao servidor de e-mail e possa buscar uma lista de pastas do sistema. Após rever a seleção de pasta do sistema, você pode adicionar a conta tocando em *Salvar*.

Se o seu provedor não estiver na lista de provedores, há milhares de outros, selecione *Personalizado*. Digite o nome do domínio, por exemplo *gmail.com* e toque em *Obter configurações*. Se o seu provedor oferece suporte para [ descoberta automática ](https://tools.ietf.org/html/rfc6186), FairEmail preencherá o nome do host e o número da porta, caso contrário, verifique as instruções de configuração do seu provedor para o nome de host IMAP correto, número da porta e protocolo de criptografia (SSL / TLS ou STARTTLS). Para saber mais sobre isso, por favor clique [aqui](https://github.com/M66B/FairEmail/blob/master/FAQ.md#authorizing-accounts).

## Configurar identidade - para enviar e-mails

Da mesma forma, para adicionar uma identidade, toque em *configuração manual e mais opções*, toque em *identidades* e toque no botão 'mais' na parte inferior. Digite o nome que você deseja que apareça no remetente dos e-mails que você envia e selecione uma conta vinculada. Toque em *Salvar* para adicionar a identidade.

Se a conta foi configurada manualmente, provavelmente você precisará configurar a identidade manualmente também. Digite o nome do domínio, por exemplo *gmail.com* e toque em *Obter configurações*. Se o seu provedor oferece suporte para [ descoberta automática ](https://tools.ietf.org/html/rfc6186), FairEmail preencherá o nome do host e o número da porta, caso contrário, verifique as instruções de configuração do seu provedor para obter o nome do host SMTP correto, o número da porta e o protocolo de criptografia (SSL / TLS ou STARTTLS).

Veja [este FAQ](https://github.com/M66B/FairEmail/blob/master/FAQ.md#FAQ9) sobre o uso de apelidos (aliases).

## Conceder permissões - para acessar informações de contato

Se você quiser consultar endereços de e-mail, exibir fotos de contatos, etc, você precisará conceder permissão para ler informações de contato para FairEmail. Toque em *Conceder* e selecione *Permitir*.

## Configurar otimizações de bateria - para receber e-mails continuamente

Nas versões recentes do Android, o Android colocará aplicativos para hibernar quando a tela estiver desligada por algum tempo para reduzir o uso de bateria. Se você deseja receber novos e-mails sem atrasos, você deve desativar as otimizações de bateria para o FairEmail. Toque em *Gerenciar* e siga as instruções.

## Perguntas ou problemas

Se você tiver dúvidas ou problemas, por favor [clique aqui](https://github.com/M66B/FairEmail/blob/master/FAQ.md) para obter ajuda.