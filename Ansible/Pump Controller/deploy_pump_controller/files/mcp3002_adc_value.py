# this script exists on the rapberrypi Desktop and is the source of this script usage
from gpiozero import MCP3002

reading = MCP3002(0);
print(reading.value*1000);

