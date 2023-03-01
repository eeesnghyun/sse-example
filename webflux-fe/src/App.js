import './App.css';

function init() {
    const connectStore = () => {
      const storeCode = document.getElementById("storeCode").value;
      alert(storeCode);
    };

    return (      
    <div>      
      <h2>가게 코드 : <input type='text' id='storeCode' placeholder='가게 코드를 입력해주세요.'/></h2>    
      <button type="button" onClick={connectStore}>접속</button>    
    </div>        
  );
}

export default init;
