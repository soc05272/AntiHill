// 채팅 대기실

import java.util.*;

class WaitRoom
{
  // final 참고링크 :  https://sabarada.tistory.com/148
  /* final : - 변수에 final을 붙이면 이 변수는 수정할 수 없다는 의미
		   	 - 수정 할 수 없다는 범위는 그 변수의 값에 한정*/

  private static final int MAX_ROOM = 10; // 만들 수 있는 최대 방 개수 : 10개
  private static final int MAX_USER = 100; // 이용 가능한 최대 인원 : 100명
  private static final String SEPARATOR = "|";
  private static final String DELIMETER = "'";
  private static final String DELIMETER1 = "=";
  
  //상황별 코드 Number
  private static final int ERR_ALREADYUSER = 3001; // 이미 사용 중인 USER_Code 
  private static final int ERR_SERVERFULL = 3002; // 방 최대치 초과 Code
  private static final int ERR_ROOMSFULL = 3011; // 방 만들기 오류 Code
  private static final int ERR_ROOMERFULL = 3021; // 비밀번호 ERROR_Code
  private static final int ERR_PASSWORD = 3022;

  private static Vector userVector, roomVector; // 사용자와 방은 벡터로 초기화
  // HashTable은 키와 값을 1:1형태로 가져가며 HashTable에 저장 →  키는 값을 식별하기 위한 고유한 키, 값은 키가 가진 값을 의미
  // https://crazykim2.tistory.com/589
  private static Hashtable userHash, roomHash; // 한 user가 하나의 room을 가지도록 HashTable로도 변수선언  

  private static int userCount; // 사용자 수
  private static int roomCount; // 방 개수 

  static{
    userVector = new Vector(MAX_USER);
    roomVector = new Vector(MAX_ROOM);
    userHash = new Hashtable(MAX_USER);
    roomHash = new Hashtable(MAX_ROOM);
    userCount = 0;
    roomCount = 0;
  }

  public WaitRoom(){}

  // 사용자 추가 시 동시 접근 문제를 해결하기 위해 쓰레드 동기화
  public synchronized int addUser(String id, ServerThread client){
    if(userCount == MAX_USER) return ERR_SERVERFULL;

    // 스레드에 안전한 구조로 사용하기 위해서 Enumeration을 사용
    // https://tiboy.tistory.com/481
    Enumeration ids = userVector.elements(); // 사용자 한 사람 한 사람
    while(ids.hasMoreElements()){
      String tempID = (String) ids.nextElement();
      if (tempID.equals(id)) return ERR_ALREADYUSER; // 닉네임 중복 시 알림
    }
    Enumeration rooms = roomVector.elements(); // 채팅 방 하나하나
    while(rooms.hasMoreElements()){
      ChatRoom tempRoom = (ChatRoom) rooms.nextElement();
      if(tempRoom.checkUserIDs(id)) return ERR_ALREADYUSER;
    }

    userVector.addElement(id);
    userHash.put(id, client);
    client.st_ID = id;
    client.st_roomNumber = 0;
    userCount++;

    return 0;
  }

  //사용자 나갔을 때 
  public synchronized void delUser(String id){
    userVector.removeElement(id);
    userHash.remove(id);
    userCount--;
  }

  // 채팅 방 입장
  public synchronized String getRooms(){
    StringBuffer room = new StringBuffer(); // StringBuffer는 문자열을 추가하거나 변경 할 때 주로 사용하는 자료형
    String rooms;
    Integer roomNum;
    Enumeration enu = roomHash.keys();
    while(enu.hasMoreElements()){
      roomNum = (Integer) enu.nextElement();
      ChatRoom tempRoom = (ChatRoom) roomHash.get(roomNum);
      room.append(String.valueOf(roomNum));
      room.append(DELIMETER1);
      room.append(tempRoom.toString());
      room.append(DELIMETER);
    }
    try{
      rooms = new String(room);
      rooms = rooms.substring(0, rooms.length() - 1);
    }catch(StringIndexOutOfBoundsException e){
      return "empty";
    }
    return rooms;
  }

  // 사용자 추가  
  public synchronized String getUsers(){
    StringBuffer id = new StringBuffer(); 
    String ids;
    Enumeration enu = userVector.elements();
    while(enu.hasMoreElements()){
      id.append(enu.nextElement());
      id.append(DELIMETER);
    }
    try{
      ids = new String(id);
      ids = ids.substring(0, ids.length() - 1);
    }catch(StringIndexOutOfBoundsException e){
      return "";
    }
    return ids;
  }

 //채팅 방 추가
  public synchronized int addRoom(ChatRoom room){
    if (roomCount == MAX_ROOM) return ERR_ROOMSFULL; // 채팅 방 만석일 경우 알림

    roomVector.addElement(room);
    roomHash.put(new Integer(ChatRoom.roomNumber), room);
    roomCount++;
    return 0;
  }
    
  public String getWaitRoomInfo(){
    StringBuffer roomInfo = new StringBuffer();
    roomInfo.append(getRooms());
    roomInfo.append(SEPARATOR);
    roomInfo.append(getUsers());
    return roomInfo.toString();
  }

//방 입장 : 사용자 간의 동시 접근 시 혼동을 방지하기 위해 쓰레드 동기화
  public synchronized int joinRoom(String id, ServerThread client,
                                   int roomNumber, String password){
    Integer roomNum = new Integer(roomNumber);
    ChatRoom room = (ChatRoom) roomHash.get(roomNum);
    if (room.isRocked()){
      if (room.checkPassword(password)){
        if (!room.addUser(id, client)){
           return ERR_ROOMERFULL;
        }
      } else {
        return ERR_PASSWORD;
      }
    } else if (!room.addUser(id, client)){
      return ERR_ROOMERFULL;
    }
    userVector.removeElement(id);
    userHash.remove(id);
   
    return 0;
  }

  public String getRoomInfo(int roomNumber){
    Integer roomNum = new Integer(roomNumber);
    ChatRoom room = (ChatRoom) roomHash.get(roomNum);
    return room.getUsers();
  }

  public synchronized boolean quitRoom(String id, int roomNumber,
                       ServerThread client){
    boolean returnValue = false;
    Integer roomNum = new Integer(roomNumber);
    ChatRoom room = (ChatRoom) roomHash.get(roomNum);
    if (room.delUser(id)){
      roomVector.removeElement(room); // 채팅 방에서 나가면 채팅 방 목록에서 그 방 지우기
      roomHash.remove(roomNum);
      roomCount--; // 방 개수 차감
      returnValue = true;
    }
    userVector.addElement(id);
    userHash.put(id, client);
    return returnValue;
  }

  public synchronized Hashtable getClients(int roomNumber){
    if (roomNumber == 0) return userHash;

    Integer roomNum = new Integer(roomNumber);
    ChatRoom room = (ChatRoom) roomHash.get(roomNum);
    return room.getClients();
  }
}



/*public class WaitRoom {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}*/
