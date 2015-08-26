function result= FrameVerification()
warning off all
host = '/Users/Guo/Desktop/Nexus_2/Nexus_2_guo_semester/';
target1 = '/Users/Guo/Desktop/Nexus_2/Nexus_2_guo_semester/';
target2 = '/Users/Guo/Desktop/Nexus_3/Nexus_3_guo_semester/';
final = '.wav';
index = 1;
k =16;
num = 20;
train = 50;

%%Get mean code book of all samples
for x = 1:train
    [y,Fs,bits] = wavread(strcat(host,int2str(x),final));
    v = mfcc(y,Fs);
    initial{x} = v;
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
disp('Sum is calculated');


mean = zeros(20,k);
for xx=1:20
    for yy= 1:k
        mean(xx,yy) = sum(xx,yy)/50;
    end
end

total = 0;
for yy= 1:k
    if(isnan(mean(1,yy))==0)
        total = total+1;
    end
end
disp(total);

for xx = 1:k
    th{xx} = [];
end
%%Get all distance from origin to meancode
for zz = 1:train
    temp = initial{zz};
    for xx = 1:size(temp,2)
        frame = temp(:,xx);
        ii = 0;
        mindist = 2000;
        for yy=1:k
            dist = mydistance(frame,mean(:,yy),2);
            if(dist<mindist)
                ii=yy;
                mindist = dist;
            end
        end
        th{ii} = [th{ii},mydistance(frame,mean(:,ii),2)];
    end
end

% for xx = 1:k
%     if size(th{xx}) ~=0
%         thf(xx) = max(th{xx});
%     else
%         thf(xx) =0;
%     end
% end

%Fit
for yy = 1:k
    if size(th{yy}) ~=0
    [n,xout]=hist(th{yy},11);
    f = fit(xout',n','gauss1');
    x= 0:0.05:15;
    y =  f.a1*exp(-((x-f.b1)/f.c1).^2);
    b = f.b1;
    plot(x,y,'r',xout,n,'b');
    tt{yy} = b+2*f.c1; 
    ttl{yy} = b-2*f.c1;
    else
        tt{yy} = 0;
        ttl{yy} = 0;
    end
end
tt


sameperson=0;
for nn = 1:99
    match =0;
    [y,Fs,bits] = wavread(strcat(target1,int2str(nn),final));
    v = mfcc(y,Fs);
    for xx = 1:size(v,2)
        frame = v(:,xx);
        ii = 0;
        mindist = 2000;
        for yy = 1:k
            dist = mydistance(frame,mean(:,yy),2);
            if(dist<mindist)
                ii=yy;
                mindist = dist;
            end
        end
        if mydistance(frame,mean(:,ii),2)<= tt{ii} && mydistance(frame,mean(:,ii),2)>= ttl{ii}
            match = match+1;
        end
    end

    if match>=size(v,2)*0.95
        sameperson = sameperson+1;
    end
            
end
disp('sameperson: ')
display(sameperson);

%%next person
sameperson=0;
for nn = 1:99
    match =0;
    [y,Fs,bits] = wavread(strcat(target2,int2str(nn),final));
    v = mfcc(y,Fs);
    for xx = 1:size(v,2)
        frame = v(:,xx);
        ii = 0;
        mindist = 2000;
        for yy = 1:k
            dist = mydistance(frame,mean(:,yy),2);
            if(dist<mindist)
                ii=yy;
                mindist = dist;
            end
        end
        if mydistance(frame,mean(:,ii),2)<= tt{ii} && mydistance(frame,mean(:,ii),2)>= ttl{ii}
            match = match+1;
        end
    end
    
    if match>=size(v,2)*0.95
        sameperson = sameperson+1;
    end
            
end
disp('diffperson: ')
display(sameperson);
%% Do the tests 




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
