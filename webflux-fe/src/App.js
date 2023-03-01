import './App.css';

function init() {
    const connectStore = () => {
      const storeCode = document.getElementById("storeCode").value;
      if (storeCode === "") return;
      
      const eventSource = new EventSource(`http://localhost:8080/notify/connect/${storeCode}`);
    
      eventSource.onopen = (e) => {
          //console.log(e);
      };	
      eventSource.onerror = (e) => {
          //console.log(e);
      };	
      eventSource.onmessage = (e) => {
        console.log(e.data);
      };
    };

    return (      
    <div>   
      <div>
        <label>가게 코드 : </label><input type='text' id='storeCode' placeholder='가게 코드를 입력해주세요.'/>
        <button type="button" onClick={connectStore}>접속</button>    
      </div>   
      <div>
        <div id="messageArea"></div>
      </div>
    </div>            
  );
}

export default init;
