local readyKey = KEYS[1]
local activeKey = KEYS[2]
local attemptsKey = KEYS[3]
local leasems = tonumber(ARGV[1])

local time = redis.call('TIME')
local ms = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)

local job = redis.call("RPOP", readyKey)

if not job then
    return nil
end

local score = redis.call("ZSCORE", attemptsKey, job)

if not score then
    redis.call("ZADD", attemptsKey, 1, job)

else
    redis.call("ZADD", attemptsKey, tonumber(score) + 1, job)
end

redis.call("ZADD", activeKey, ms + leasems, job)
return job
