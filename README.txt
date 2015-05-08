ArduinoController PROJECT by TwnET

**Change log**
v1.0
*20150508

已完成
1.建立藍芽配對Activity完整連接函式庫(待取得Arduino藍芽模組後修改BluetoothServer class)
2.布局完成藍芽配對Activity XML

未完成
1.按鈕聆聽器收到事件後的動作
2.藍芽IO協議未完成(待Arduino完成藍芽接收程序)
	a.UUID待修改
	b.藍芽送出的資料 細節未定義
	c.須配合藍芽模組之配對程序，也許不必使用AcceptThread class 只需單方向使用ConnectThread
3.接收Arduino狀態之程序與顯示狀態的UI介面
4.完善UI操作介面