local leaderKey = KEYS[1]
local ownerId = ARGV[1]
local delay = tonumber(ARGV[2])

local isLocked = redis.call("EXISTS", leaderKey);

if isLocked == 0 then
    return 0
end

local currentOwner = redis.call("Get", leaderKey);

if currentOwner ~= ownerId then
    return 0
end

local status = redis.call("PEXPIRE", leaderKey, delay);

return status
