
local socket = require("socket.core")

ButtonNames = {
    "A",
	"B",
	"up",
	"down",
	"left",
	"right",
}


BoxRadius = 6
InputSize = (BoxRadius*2+1)*(BoxRadius*2+1)
OutputSize = #ButtonNames

LastPostitionX = 0
LastPostitionXUnchangedCount = 0

LastPositionY = 0

function getPositions()
    marioX = memory.readbyte(0x6D) * 0x100 + memory.readbyte(0x86)
    marioY = memory.readbyte(0x03B8)+16
       
    screenX = memory.readbyte(0x03AD)
    screenY = memory.readbyte(0x03B8)
end

function getTile(dx, dy)
        
	local x = marioX + dx + 8
	local y = marioY + dy - 16
	local page = math.floor(x/256)%2

	local subx = math.floor((x%256)/16)
	local suby = math.floor((y - 32)/16)
	local addr = 0x500 + page*13*16+suby*16+subx
   
	if suby >= 13 or suby < 0 then
			return 0
	end
   
	if memory.readbyte(addr) ~= 0 then
			return 1
	else
			return 0
	end
end

function getSprites()
	local sprites = {}
	for slot=0,4 do
		local enemy = memory.readbyte(0xF+slot)
		if enemy ~= 0 then
			local ex = memory.readbyte(0x6E + slot)*0x100 + memory.readbyte(0x87+slot)
			local ey = memory.readbyte(0xCF + slot)+24
			sprites[#sprites+1] = {["x"]=ex,["y"]=ey}
		end
	end
   
	return sprites
end

function getInputs()
	getPositions()
   
	sprites = getSprites()
   
	local inputs = {}
   
	for dy=-BoxRadius*16,BoxRadius*16,16 do
		for dx=-BoxRadius*16,BoxRadius*16,16 do
			inputs[#inputs+1] = 0
		   
			tile = getTile(dx, dy)
			if tile == 1 and marioY+dy < 0x1B0 then
				inputs[#inputs] = 1
			end
		   
			for i = 1,#sprites do
				distx = math.abs(sprites[i]["x"] - (marioX+dx))
				disty = math.abs(sprites[i]["y"] - (marioY+dy))
				if distx <= 8 and disty <= 8 then
					inputs[#inputs] = -1
				end
			end
		end
	end
  
	return inputs
end

function clearJoypad()
	controller = {}
	for b = 1,#ButtonNames do
			controller[ButtonNames[b]] = false
	end
	joypad.set(1, controller)
end

function updateJoypad(output)
	controller = {}
	for b = 1,#ButtonNames do
			controller[ButtonNames[b]] = (output:sub(b, b) == "1")
	end
	joypad.set(1, controller)
end
 
function restart()
	LastPostition = 0
	LastPostitionUnchangedCount = 0
	local obj = savestate.object(1)
	savestate.load(obj)
	clearJoypad()
end

function prepareConnection()
	master = socket.tcp()
	master:connect('localhost', 1024)
	master:send(tostring(InputSize) .. "\n")
	master:send(tostring(OutputSize) .. "\n")
end
 
function inputsPayload(inputs)
	master:send("I")
	for i = 1,#inputs do
		if inputs[i] == -1 then
			master:send("e")
		elseif inputs[i] == 1 then
			master:send("b")
		else
			master:send("s")
		end
	end
end

function getOutputResponse()
	local line, err = master:receive()
	return line
end

function fitnessPayload()
	master:send("F")
	master:send(tostring(marioX) .. "\n");
end

--prepareConnection() 

while true do
		
	inputs = getInputs()

	--inputsPayload(inputs)
	--output = getOutputResponse()
	--updateJoypad(output)
	
	--Check if mario hasn't moved horizontally
	if(LastPostitionX >= marioX) then
		LastPostitionXUnchangedCount = LastPostitionXUnchangedCount + 1
	else
		LastPostitionX = marioX
	end	
	
	

	if LastPostitionXUnchangedCount >= 60 then
		restart()
	end
	
	emu.frameadvance()
	
end