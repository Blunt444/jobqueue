local activeKey = KEYS[1]
local attemptsKey = KEYS[2]
local job = ARGV[1]
local fencingToken = tonumber(ARGV[2])

local actualFencingToken = redis.call("ZSCORE", attemptsKey, job)

if not actualFencingToken or fencingToken ~= tonumber(actualFencingToken) then
    return 0
end

local status = redis.call("ZREM", activeKey, job)

if status == 1 then
    redis.call("ZREM", attemptsKey, job)
end

return status
