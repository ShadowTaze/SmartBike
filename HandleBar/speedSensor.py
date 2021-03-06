import RPi.GPIO as GPIO

class speedSensor(object):
	#Initialize the GPIO.
	def __init__(self, port=21):
		self.port = port
		GPIO.setmode(GPIO.BOARD)
	`	GPIO.setup(port, GPIO.IN) #speed

	#Returns the value of the sensor connected to port 21.
	def sensorVal():
		return GPIO.input(self.port)
