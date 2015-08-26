function result= Pixel_Algorithm()
warning off all
host = '/Users/Guo/Desktop/Nexus_3/Nexus_3_guo_semester/';
target1 = '/Users/Guo/Desktop/Nexus_3/Nexus_3_guo_semester/';
target2 = '/Users/Guo/Desktop/Nexus_2/Nexus_2_guo_semester/';
final = '.wav';
index = 1;
k =16;
num = 20;
train = 50;

%%Get mean code book of all samples
for x = 1:train
    [y,Fs,bits] = wavread(strcat(host,int2str(x),final));
    v = mfcc(y,Fs);
    precode{x} = vqlbg(v,k);
end 


sum = zeros(20,k);
for xx = 1:20
    for yy = 1:k
        for m= 1:train
        sum(xx,yy)=sum(xx,yy)+precode{m}(xx,yy);
        end
    end
end


total = 0;
mean = zeros(20,k);
for xx=1:20
    for yy= 1:k
        mean(xx,yy) = sum(xx,yy)/50;
        if(isnan(mean(xx,yy))==0)
            total = total+1;
        end;
    end
end
disp(total);

dist = zeros(train,20,k);
%%Get all distance from pixel to pixel
for zz = 1:train
    for xx = 1:20
        for yy= 1:k
            dist(zz,xx,yy) = precode{zz}(xx,yy)-mean(xx,yy);
        end
    end
end
disp('distance calcualted finished');


for xx = 1:20
    for yy = 1:k
    th(xx,yy) = max(dist(:,xx,yy));
    thl(xx,yy) = min(dist(:,xx,yy));
    end
end

for xx=1:20
    for yy =1:k
        A{xx,yy} = [];
    end
end

%Test target 1
sameperson=0;
for nn = 1:99
    match =0;
    [y,Fs,bits] = wavread(strcat(target1,int2str(nn),final));
    v = mfcc(y,Fs);
    d = vqlbg(v,k);
    for xx =1:20
        for yy = 1:k
            distance = d(xx,yy)-mean(xx,yy);
            if(distance<=th(xx,yy)&&distance>=thl(xx,yy))
                match = match+1;
            end
        end
    end
    if(match>=total*0.95)
        sameperson=sameperson+1;
    end
end
disp('sameperson:');
disp(sameperson);


%Test on Target 2
sameperson=0;
for nn = 1:99
    match =0;
    [y,Fs,bits] = wavread(strcat(target2,int2str(nn),final));
    v = mfcc(y,Fs);
    d = vqlbg(v,k);
    for xx =1:20
        for yy = 1:k
            distance = d(xx,yy)-mean(xx,yy);
            if(distance<=th(xx,yy)&&distance>=thl(xx,yy))
                match = match+1;
            end
        end
    end
    if(match>=total*0.95)
        sameperson=sameperson+1;
    end
end
disp('diffperson:');
disp(sameperson);





%%
function m = melfb(p, n, fs)

f0 = 700 / fs;
fn2 = floor(n/2);

lr = log(1 + 0.5/f0) / (p+1);

% convert to fft bin numbers with 0 for DC term
bl = n * (f0 * (exp([0 1 p p+1] * lr) - 1));

b1 = floor(bl(1)) + 1;
b2 = ceil(bl(2));
b3 = floor(bl(3));
b4 = min(fn2, ceil(bl(4))) - 1;

pf = log(1 + (b1:b4)/n/f0) / lr;
fp = floor(pf);
pm = pf - fp;

r = [fp(b2:b4) 1+fp(1:b3)];
c = [b2:b4 1:b3] + 1;
v = 2 * [1-pm(b2:b4) pm(1:b3)];

m = sparse(r, c, v, p, 1+fn2);

%%
function M3 = blockFrames(s, fs, m, n)

l = length(s);
nbFrame = floor((l - n) / m) + 1;

for i = 1:n
    for j = 1:nbFrame
        M(i, j) = s(((j - 1) * m) + i);
    end
end

h = hamming(n);
M2 = diag(h) * M;

for i = 1:nbFrame
    M3(:, i) = fft(M2(:, i));
end

%%
function d = disteu(x, y)

[M, N] = size(x);
[M2, P] = size(y);

if (M ~= M2)
    error('Matrix dimensions do not match.')
end

d = zeros(N, P);


for ii=1:N
    for jj=1:P
        d(ii,jj) = mydistance(x(:,ii),y(:,jj),2);
    end
end

%%
function r = mfcc(s, fs)

m = 100;
n = 256;
l = length(s);

nbFrame = floor((l - n) / m) + 1;

for i = 1:n
    for j = 1:nbFrame
        M(i, j) = s(((j - 1) * m) + i);
    end
end

h = hamming(n);

M2 = diag(h) * M;

for i = 1:nbFrame
    frame(:,i) = fft(M2(:, i));
end

t = n / 2;
tmax = l / fs;

m = melfb(20, n, fs);
n2 = 1 + floor(n / 2);
z = m * abs(frame(1:n2, :)).^2;

r = dct(log(z));

%%
function [out] = mydistance(x,y,tipo)

if tipo == 0
    out = sum((x-y).^2).^0.5;
end
if tipo == 1
    out = sum(abs(x-y));
end

if tipo == 2
    pesi = zeros(size(x));
    pesi(1)     = 0.20;
    pesi(2)     = 0.90;
    pesi(3)     = 0.95;
    pesi(4)     = 0.90;
    pesi(5)     = 0.70;
    pesi(6)     = 0.90;
    pesi(7)     = 1.00;
    pesi(8)     = 1.00;
    pesi(9)     = 1.00;
    pesi(10)    = 0.95;
    pesi(11:13) = 0.30;
    
    out = sum(abs(x-y).*pesi);
end

%%
function r = vqlbg(d,k)
e   = .01;
r   = mean(d, 2);
dpr = 10000;

for i = 1:log2(k)
    r = [r*(1+e), r*(1-e)];
    
    while (1 == 1)
        z = disteu(d, r);
        [m,ind] = min(z, [], 2);
        t = 0;
        for j = 1:2^i
            r(:, j) = mean(d(:, find(ind == j)), 2);
            x = disteu(d(:, find(ind == j)), r(:, j));
            for q = 1:length(x)
                t = t + x(q);
            end
        end
        if (((dpr - t)/t) < e)
            break;
        else
            dpr = t;
        end
    end
end
