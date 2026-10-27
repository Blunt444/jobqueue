local activeKey = KEYS[1]
local job = ARGV[1]

local status = redis.call("ZREM", activeKey, job)

return status