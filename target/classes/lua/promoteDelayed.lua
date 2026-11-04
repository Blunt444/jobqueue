local readyKey = KEYS[1]
local delayKey = KEYS[2]
local batchSize = tonumber(ARGV[1])

local time = redis.call("TIME")
local ms = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)

local delayedJobs = redis.call(
    "ZRANGEBYSCORE",
    delayKey,
    "-inf",
    ms,
    "LIMIT", 0, batchSize
)

local count = 0

for _,job in ipairs(delayedJobs) do
    if redis.call("ZREM", delayKey, job) == 1 then
        redis.call("LPUSH",readyKey,job)
        count = count + 1
    end
end

return count