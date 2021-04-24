import socket

host = ''
port = 8001

server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #소켓 객체 생성(IPv4, TCP)
server_sock.bind((host,port)) # IP, PORT 연결
server_sock.listen() # 클라이언트 접속 허용
print('Listening..')

client_socket, addr = server_sock.accept() # 클라이언트 접속 시 새로운 소켓 리턴
print('Connected by',addr)

while True:
    recv_data = client_socket.recv(1024) # 데이터 수신 대기

    if not recv_data:
        break
    
    data = recv_data.decode()
    print(data)
    client_socket.sendall(recv_data) # 받은 데이터 다시 클라이언트에게 전송

client_socket.close()
server_sock.close()
