# Projeto_1_SC_fase_2

By SegC-027 Group :
- Alexandre Müller - FC56343
- Diogo Ramos - FC56308
- Francisco Henriques - FC56348 


--Português--

# Descrição
Sistema Tintolmarket, um sistema do tipo cliente-servidor que oferece um serviço semelhante ao do Vivino, mas permitindo a compra e venda de vinhos por parte dos utilizadores do sistema. 

# Como correr
    
    1. Abrir um terminal dentro da pasta onde estão colocados os jars
    2. Correr o jar TintolmarketServer com <port> <password-cifra> <keystore> <password-keystore>
    3. Colocar as imagens dos vinhos que serão posteriormente adicionados no diretório client_files
    4. Correr o jar Tintolmarket com <serverAddress> <truststore> <keystore> <password-keystore> <userID>
    5. Escrever o comando pretendido e no fim de cada comando carregar na tecla ENTER para executar outro comando.
    6. Para terminar o programa cliente executar a combinação de teclas CTRL+C
    7. Executar também o passo 6 para terminar o programa servidor

    Exemplo:

        1. Abrir diretório onde estão os ficheiros jar
        2. Abrir um terminal e executar comando "java -jar TintolmarketServer.jar 12345 adminadmin keystore.server adminadmin" 
        3. Colocar uma imagem no diretório client_files
        4. Abrir um terminal e executar comando "java -jar Tintolmarket.jar localhost:12345 truststore.client keystore.client1 adminadmin client1"        
        5. No final para terminar os programas executar a combinação de teclas CTRL+C
