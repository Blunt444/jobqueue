local activeKey = KEYS[1]
local delay = tonumber(ARGV[1])
local job = ARGV[2]

local score = redis.call("ZSCORE", activeKey, job)

if not score then
    return 0
end

local time = redis.call("TIME")
local ms = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)

local newDeadline = ms + delay;

redis.call("ZADD", activeKey, newDeadline, job)

return 1