import socket
import _thread

def new_client(client_socket, addr, group):
    while True:
        try:
            recv_data = client_socket.recv(1024) # 데이터 수신 대기

            if not recv_data:
                print('Disconnected by',addr)
                group.remove(client_socket)
                break
            
            data = recv_data.decode()
            print(data)

            for c in group:
                c.sendall(recv_data) # 받은 데이터 다시 클라이언트에게 전송
        except:
            # 클라이언트 소켓 강제 종료 시 (ex : 네트워크 변경)
            print('예외발생')
            print('Disconnected by',addr)
            group.remove(client_socket)
            break
    client_socket.close()


host = ''
port = 8001

server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #소켓 객체 생성(IPv4, TCP)
server_sock.bind((host,port)) # IP, PORT 연결
server_sock.listen() # 클라이언트 접속 허용
print('Listening..')

group = []
while True:
    conn, addr = server_sock.accept() # 클라이언트 접속 시 새로운 소켓 리턴
    group.append(conn)
    print('Connected by',addr)
    _thread.start_new_thread(new_client,(conn, addr, group)) # 클라이언트 스레드 생성

server_sock.close()
