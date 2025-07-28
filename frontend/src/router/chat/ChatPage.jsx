import React, { useEffect, useState, useCallback, useRef } from "react";
import toast from "react-hot-toast";
import { useParams } from "react-router-dom";
import { Client } from "@stomp/stompjs";
import "../../style/ChatPage.css";

import axiosInstance from "../../lib/axios.js";

/*

    // ✅ Content!! 에서 채팅방 생성하는 함수
    // 채팅방으로 이동 << 주석이 달린 곳에 여기 ChatPage경로를 주면 됨

    const handleCreateChatRoom = async (targetEmail) => {
      try {
        const res = await api.post("/chat/rooms/find-or-create", {
          targetEmail: targetEmail,
        });

        const room = res.data; // ChatRoomDto
        navigate(`/chat/${room.roomId}`); // ✅ 채팅방으로 이동
      } catch (err) {
        console.error("❌ 채팅방 생성 실패:", err);
        toast.error("채팅방 생성 실패");
      }
    };
*/

const ChatPage = () => {
  const { roomId: initialRoomId } = useParams();
  const [rooms, setRooms] = useState([]);
  const [currentRoom, setCurrentRoom] = useState(initialRoomId || null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState("");
  const [stompClient, setStompClient] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const [myEmail, setMyEmail] = useState(null);

  const [userInfo, setUserInfo] = useState(null);
  const [isUserLoading, setIsUserLoading] = useState(true);

  const messagesEndRef = useRef(null);

  useEffect(() => {
    axiosInstance
        .get("/api/me")
        .then((res) => {
          setUserInfo(res.data);
          setMyEmail(res.data.email);
        })
        .catch((err) => {
          console.error("사용자 정보 불러오기 실패:", err);
          toast.error("사용자 정보를 불러오지 못했습니다.");
        })
        .finally(() => setIsUserLoading(false));
  }, []);

  // ✅ 스크롤 최신 메시지로 이동
  const scrollToBottom = () => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const fetchChatRooms = useCallback(async () => {
    try {
      const res = await axiosInstance.get("/api/chat/rooms");
      setRooms(res.data);
    } catch (err) {
      console.error("❌ 채팅방 리스트 불러오기 실패:", err);
    }
  }, []);

  useEffect(() => {
    if (!isUserLoading && userInfo?.email) {
      fetchChatRooms();
    }
  }, [isUserLoading, userInfo, fetchChatRooms]);

  const fetchMessages = useCallback(async (roomId) => {
    if (!roomId || roomId === "undefined" || isNaN(Number(roomId))) return;
    try {
      const res = await axiosInstance.get(`/api/chat/rooms/${roomId}/messages`);
      setMessages(res.data);
    } catch (err) {
      console.error("❌ 메시지 불러오기 실패:", err);
    }
  }, []);

  const connectWebSocket = useCallback(
      (roomId) => {
        if (!roomId || roomId === "undefined" || isNaN(Number(roomId))) return;

        if (stompClient) stompClient.deactivate();

        const client = new Client({
          brokerURL: "ws://localhost:8080/ws/chat",
          connectHeaders: {
            Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`,
          },
          reconnectDelay: 5000,
          onConnect: () => {
            setIsConnected(true);

            client.subscribe(`/sub/chatroom/${roomId}`, (msg) => {
              const newMessage = JSON.parse(msg.body);
              setMessages((prev) => [...prev, newMessage]);
            });
          },
        });

        client.activate();
        setStompClient(client);
      },
      [stompClient]
  );

  const sendMessage = () => {
    if (!stompClient || !stompClient.connected || !isConnected) return;
    if (!inputMessage.trim() || !currentRoom) return;

    const payload = { message: inputMessage };

    stompClient.publish({
      destination: `/pub/chat/${currentRoom}.send`,
      body: JSON.stringify(payload),
    });
    setInputMessage("");
  };

  const handleSelectRoom = (roomId) => {
    if (!roomId || roomId === "undefined" || isNaN(Number(roomId))) return;

    setCurrentRoom(roomId);
    fetchMessages(roomId);
    connectWebSocket(roomId);
  };

  useEffect(() => {
    if (initialRoomId && initialRoomId !== "undefined" && !isNaN(Number(initialRoomId))) {
      handleSelectRoom(initialRoomId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    return () => {
      if (stompClient) stompClient.deactivate();
    };
  }, [stompClient]);

  if (isUserLoading) return <div>Loading user info...</div>;

  // Helper function to group messages by sender and time slot
  const groupMessages = (msgs) => {
    const groups = [];
    let currentGroup = null;
    msgs.forEach((msg) => {
      const time = new Date(msg.sendAt);
      const timeSlot = `${time.getFullYear()}-${time.getMonth()}-${time.getDate()} ${time.getHours()}:${time.getMinutes()}`;
      if (!currentGroup || currentGroup.senderEmail !== msg.senderEmail || currentGroup.timeSlot !== timeSlot) {
        if (currentGroup) groups.push(currentGroup);
        currentGroup = {
          senderEmail: msg.senderEmail,
          timeSlot,
          messages: [msg],
        };
      } else {
        currentGroup.messages.push(msg);
      }
    });
    if (currentGroup) groups.push(currentGroup);
    return groups;
  };

  const handleKeyDown = (e) => {
    // ✅ 한글 조합 중 Enter 무시
    if (e.nativeEvent.isComposing) return;

    if (e.key === "Enter") {
      e.preventDefault(); // Enter 기본 동작 방지
      sendMessage();
    }
  };

  return (
      <div className="container mb-8">
        {/* ✅ 좌측: 채팅방 리스트 */}
        <div className="sidebar">
          <h2 className="sidebarTitle">내 채팅방</h2>
          <ul className="roomList">
            {rooms.map((room) => (
                <li key={room.roomId} className={`roomItem${room.roomId === currentRoom ? " activeRoom" : ""}`} onClick={() => handleSelectRoom(room.roomId)}>
                  <img src={room.opponentProfileImage || "/default-profile.png"} alt="상대 프로필" className="roomAvatar" />
                  <div style={{ flex: 1 }}>
                    <div className="roomName">{room.opponentName || `채팅방 #${room.roomId}`}</div>
                    <div className="roomLastMsg">{room.lastMessage || "최근 메시지 없음"}</div>
                  </div>
                  {room.unreadCount > 0 && <span className="unreadBadge">{room.unreadCount}</span>}
                </li>
            ))}
          </ul>
        </div>

        {/* ✅ 우측: 채팅 메시지 영역 */}
        <div className="chatSection">
          {currentRoom ? (
              <>
                <div className="chatHeader">{rooms.find((r) => r.roomId === currentRoom)?.opponentName || `채팅방 #${currentRoom}`}</div>
                <div className="messagesContainer">
                  {groupMessages(messages).map((group, idx) => {
                    const isMine = group.senderEmail === myEmail;
                    const opponent = rooms.find((r) => r.roomId === currentRoom);
                    return (
                        <div
                            key={idx}
                            className="messageGroup"
                            style={{
                              alignItems: isMine ? "flex-end" : "flex-start",
                              justifyContent: isMine ? "flex-end" : "flex-start",
                            }}>
                          {!isMine && <img src={opponent?.opponentProfileImage || "/default-profile.png"} alt="상대 프로필" className="msgAvatar" />}
                          <div
                              style={{
                                maxWidth: "70%",
                                display: "flex",
                                flexDirection: "column",
                                alignItems: isMine ? "flex-end" : "flex-start",
                              }}>
                            {group.messages.map((m, i) => {
                              const isLast = i === group.messages.length - 1;
                              const time = new Date(m.sendAt).toLocaleTimeString([], {
                                hour: "2-digit",
                                minute: "2-digit",
                              });
                              return (
                                  <div key={m.chatId} style={{ marginBottom: "4px" }}>
                                    <div
                                        className="messageBubble"
                                        style={{
                                          background: isMine ? "#ffeb33" : "#fff",
                                          borderTopRightRadius: isMine ? "4px" : "16px",
                                          borderTopLeftRadius: isMine ? "16px" : "4px",
                                        }}>
                                      {m.message}
                                    </div>
                                    {isLast && <div className="messageTime">{time}</div>}
                                  </div>
                              );
                            })}
                          </div>
                        </div>
                    );
                  })}
                  <div ref={messagesEndRef} />
                </div>
                <div className="inputContainer">
                  <input type="text" placeholder="메시지를 입력하세요..." value={inputMessage} onChange={(e) => setInputMessage(e.target.value)} onKeyDown={handleKeyDown} className="inputField" />
                  <button onClick={sendMessage} className="sendButton">
                    ✈️
                  </button>
                </div>
              </>
          ) : (
              <div className="emptyChat">채팅방을 선택하세요.</div>
          )}
        </div>
      </div>
  );
};

export default ChatPage;